package ru.netology.nework.ui.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.dto.EventDto
import ru.netology.nework.repository.EventRepository
import javax.inject.Inject


data class EventUiState(
    val loading: Boolean = false,
    val events: List<EventDto> = emptyList()
)


@HiltViewModel
class EventsViewModel @Inject constructor(
    private val repository: EventRepository,
    private val appAuth: AppAuth
) : ViewModel() {

//    private val _state = MutableStateFlow(EventUiState())
//    val state: StateFlow<EventUiState> = _state.asStateFlow()

    val pagingEvents: Flow<PagingData<EventDto>> = repository.pagingEvents()
        .map { pagingData ->
            pagingData.map { event ->
                event.copy(ownedByMe = event.authorId == myId)
            }
        }.cachedIn(viewModelScope)

    private val _error = MutableSharedFlow<String>()
    val error = _error.asSharedFlow()

    private val myId: Long get() = appAuth.authState.value.id

    fun isAuthorized(): Boolean = appAuth.authState.value.token != null

//    init {
//        loadEvents()
//    }

//    fun loadEvents() {
//        viewModelScope.launch {
//            _state.update { it.copy(loading = true) }
//            repository.getEvents()
//                .onSuccess { events ->
//                    _state.update {
//                        it.copy(
//                            loading = false,
//                            events = events
//                                .sortedByDescending { event -> event.id }
//                                .map { event -> event.withOwnership() }
//                        )
//                    }
//                }
//                .onFailure { throwable ->
//                    _state.update { it.copy(loading = false) }
//                    _error.emit(throwable.message ?: "Не удалось загрузить события")
//                }
//        }
//    }

    fun like(event: EventDto) {
        viewModelScope.launch {
            val optimistic = event.copy(
                likedByMe = !event.likedByMe,
                likeOwnerIds = if (event.likedByMe) event.likeOwnerIds - myId
                else event.likeOwnerIds + myId
            )
            repository.updateDb(optimistic)
//        updateEventInList(optimistic)
            repository.like(event)
                .onSuccess { serverEvent ->
//                    updateEventInList(serverEvent.withOwnership())
                    repository.updateDb(serverEvent.copy(ownedByMe = event.ownedByMe))
                }
                .onFailure { throwable ->
//                    updateEventInList(event)
                    repository.updateDb(event)
                    _error.emit(throwable.message ?: "Не удалось изменить лайк")
                }
        }
    }

    fun participate(event: EventDto) {
        viewModelScope.launch {
            val isAlready = event.participantsIds.contains(myId)
            val optimistic = event.copy(
//                participatedByMe = !event.participatedByMe,
                participatedByMe = !event.participatedByMe,
                participantsIds = if (isAlready) event.participantsIds - myId
                else event.participantsIds + myId
            )
            repository.updateDb(optimistic)
//            updateEventInList(optimistic)
//        viewModelScope.launch {
            repository.participate(event)
                .onSuccess { serverEvent ->
//                    updateEventInList(serverEvent.withOwnership())
                    repository.updateDb(serverEvent.copy(ownedByMe = event.ownedByMe))
                }
                .onFailure { throwable ->
//                    updateEventInList(event)
                    repository.updateDb(event)
                    _error.emit(throwable.message ?: "Не удалось изменить список участников")
                }
        }
    }

    fun delete(event: EventDto) {
        viewModelScope.launch {
//            repository.deleteEvent(event.id)
//                .onSuccess {
//                    _state.update { it.copy(events = it.events.filterNot { e -> e.id == event.id }) }
            repository.deleteFromDb(event.id)
            repository.deleteEvent(event.id)
//        }
                .onFailure { throwable ->
                    _error.emit(throwable.message ?: "Не удалось удалить событие")
                }
        }
    }

//    private fun updateEventInList(event: EventDto) {
//        _state.update {
//            it.copy(events = it.events.map { e ->
//                if (e.id == event.id) event
//                else e
//            })
//        }
//    }

//    private fun EventDto.withOwnership() = copy(ownedByMe = authorId == myId)
}