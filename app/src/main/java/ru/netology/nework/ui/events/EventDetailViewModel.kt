package ru.netology.nework.ui.events

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.dto.EventDto
import ru.netology.nework.dto.UserDto
import ru.netology.nework.repository.EventRepository
import ru.netology.nework.repository.UserRepository
import javax.inject.Inject


data class EventDetailUiState(
    val event: EventDto? = null,
    val speakers: List<UserDto> = emptyList(),
    val likers: List<UserDto> = emptyList(),
    val participants: List<UserDto> = emptyList(),
    val loading: Boolean = false
)

@HiltViewModel
class EventDetailViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val userRepository: UserRepository,
    private val appAuth: AppAuth,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val eventId: Long = savedStateHandle.get<Long>("eventId") ?: 0
    private var usersById: Map<Long, UserDto> = emptyMap()

    private val _state = MutableStateFlow(EventDetailUiState())
    val state = _state.asStateFlow()

    private val _error = MutableSharedFlow<String>()
    val error = _error.asSharedFlow()

    init {
        load()
    }

    fun isAuthorized(): Boolean = appAuth.authState.value.token != null

    private fun load() {
        viewModelScope.launch {
            _state.update {
                it.copy(loading = true)
            }
            val eventResult = eventRepository.getEventById(eventId)
            val usersResult = userRepository.getUsers()

            eventResult
                .onSuccess { event ->
                    usersById = usersResult.getOrNull()?.associateBy { it.id } ?: emptyMap()
                    applyEvent(event, loading = false)
                }
                .onFailure { error ->
                    _state.update { it.copy(loading = false) }
                    _error.emit(error.message ?: "Не удалось загрузить событие")
                }
        }
    }

    fun joinEvent() {
        val current = _state.value.event ?: return
        val myId = appAuth.authState.value.id
        val optimistic = current.copy(
            participatedByMe = !current.participatedByMe,
            participantsIds = if (current.participatedByMe) current.participantsIds - myId
            else current.participantsIds + myId
        )
        applyEvent(optimistic)
        viewModelScope.launch {
            eventRepository.participate(current)
                .onSuccess { server -> applyEvent(server) }
                .onFailure { error ->
                    applyEvent(current)
                    _error.emit(error.message ?: "Не удалось обновить статус участия")
                }
        }
    }

    private fun applyEvent(event: EventDto, loading: Boolean? = null) {
        _state.update {
            it.copy(
                event = event,
                speakers = event.speakerIds.mapNotNull { id -> usersById[id] },
                likers = event.likeOwnerIds.mapNotNull { id -> usersById[id] },
                participants = event.participantsIds.mapNotNull { id -> usersById[id] },
                loading = loading ?: it.loading
            )
        }
    }
}