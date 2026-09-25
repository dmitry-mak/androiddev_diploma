package ru.netology.nework.ui.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _state = MutableStateFlow(EventUiState())
    val state: StateFlow<EventUiState> = _state.asStateFlow()

    private val _error = MutableSharedFlow<String>()
    val error = _error.asSharedFlow()

    private val myId: Long get() = appAuth.authState.value.id

    fun isAuthorized(): Boolean = appAuth.authState.value.token != null

    init {
        loadEvents()
    }
    fun loadEvents() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            repository.getEvents()
                .onSuccess { events ->
                    _state.update {
                        it.copy(
                            loading = false,
                            events = events
                                .sortedByDescending { event -> event.id }
                                .map { event -> event.withOwnership() }
                        )
                    }
                }
                .onFailure { throwable ->
                    _state.update { it.copy(loading = false) }
                    _error.emit(throwable.message ?: "Не удалось загрузить события")
                }
        }
    }

    fun like(event: EventDto) {
        val optimistic = event.copy(
            likedByMe = !event.likedByMe,
            likeOwnerIds = if (event.likedByMe) event.likeOwnerIds - myId
            else event.likeOwnerIds + myId
        )
        updateEventInList(optimistic)
        viewModelScope.launch {
            repository.like(event)
                .onSuccess { serverEvent -> updateEventInList(serverEvent.withOwnership()) }
                .onFailure { throwable ->
                    updateEventInList(event)
                    _error.emit(throwable.message ?: "Не удалось изменить лайк")
                }
        }
    }

    fun participate(event: EventDto){
        val optimistic =event.copy(
            participatedByMe = !event.participatedByMe,
            participantsIds = if (event.participatedByMe) event.participantsIds - myId
            else event.participantsIds + myId
        )
        updateEventInList(optimistic)
        viewModelScope.launch {
            repository.participate(event)
                .onSuccess { serverEvent -> updateEventInList(serverEvent.withOwnership()) }
                .onFailure { throwable ->
                    updateEventInList(event)
                    _error.emit(throwable.message ?: "Не удалось изменить список участников")
                }
        }
    }

    fun delete(event: EventDto){
        viewModelScope.launch {
            repository.deleteEvent(event.id)
                .onSuccess {
                    _state.update { it.copy(events = it.events.filterNot { e -> e.id == event.id }) }
                }
                .onFailure { throwable ->
                    _error.emit(throwable.message ?: "Не удалось удалить событие")
                }
        }
    }

    private fun updateEventInList(event: EventDto) {
        _state.update {
            it.copy(events = it.events.map { e ->
                if (e.id == event.id) event
                else e
            })
        }
    }

    private fun EventDto.withOwnership() = copy(ownedByMe = authorId == myId)
}