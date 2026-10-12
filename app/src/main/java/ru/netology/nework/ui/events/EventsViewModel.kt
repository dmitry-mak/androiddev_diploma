package ru.netology.nework.ui.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.dto.EventDto
import ru.netology.nework.error.AppError
import ru.netology.nework.error.toAppError
import ru.netology.nework.repository.EventRepository
import javax.inject.Inject


@HiltViewModel
class EventsViewModel @Inject constructor(
    private val repository: EventRepository,
    private val appAuth: AppAuth
) : ViewModel() {

    val pagingEvents: Flow<PagingData<EventDto>> = repository.pagingEvents()
        .map { pagingData ->
            pagingData.map { event ->
                event.copy(ownedByMe = event.authorId == myId)
            }
        }.cachedIn(viewModelScope)

    private val _error = MutableSharedFlow<AppError>()
    val error = _error.asSharedFlow()

    private val myId: Long get() = appAuth.authState.value.id

    fun isAuthorized(): Boolean = appAuth.authState.value.token != null

    fun like(event: EventDto) {
        viewModelScope.launch {
            val optimistic = event.copy(
                likedByMe = !event.likedByMe,
                likeOwnerIds = if (event.likedByMe) event.likeOwnerIds - myId
                else event.likeOwnerIds + myId
            )
            repository.updateDb(optimistic)
            repository.like(event)
                .onSuccess { serverEvent ->
                    repository.updateDb(serverEvent.copy(ownedByMe = event.ownedByMe))
                }
                .onFailure { throwable ->
                    repository.updateDb(event)
                    _error.emit(throwable.toAppError())
                }
        }
    }

    fun participate(event: EventDto) {
        viewModelScope.launch {
            val isAlready = event.participantsIds.contains(myId)
            val optimistic = event.copy(
                participatedByMe = !event.participatedByMe,
                participantsIds = if (isAlready) event.participantsIds - myId
                else event.participantsIds + myId
            )
            repository.updateDb(optimistic)
            repository.participate(event)
                .onSuccess { serverEvent ->
                    repository.updateDb(serverEvent.copy(ownedByMe = event.ownedByMe))
                }
                .onFailure { throwable ->
                    repository.updateDb(event)
                    _error.emit(throwable.toAppError())
                }
        }
    }

    fun delete(event: EventDto) {
        viewModelScope.launch {
            repository.deleteFromDb(event.id)
            repository.deleteEvent(event.id)
                .onSuccess {  }
                .onFailure { throwable ->
                    repository.updateDb(event)
                    _error.emit(throwable.toAppError())
                }
        }
    }
}