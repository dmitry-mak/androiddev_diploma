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
import ru.netology.nework.dto.AttachmentDto
import ru.netology.nework.dto.CoordinatesDto
import ru.netology.nework.dto.CreateEventRequest
import ru.netology.nework.dto.EventType
import ru.netology.nework.repository.EventRepository
import ru.netology.nework.repository.MediaRepository
import java.io.File
import java.time.Instant
import javax.inject.Inject


data class CreateEventUiState(
    val eventId: Long = 0L,
    val content: String = "",
    val link: String = "",
    val dateTimeMillis: Long? = null,
    val type: EventType = EventType.ONLINE,
    val speakerIds: List<Long> = emptyList(),
    val coords: CoordinatesDto?=null,
    val attachment: AttachmentDto? = null,
    val uploading: Boolean = false,
    val saving: Boolean = false
)

@HiltViewModel
class CreateEventViewModel @Inject constructor(
    private val repository: EventRepository,
    private val mediaRepository: MediaRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(CreateEventUiState())
    val state = _state.asStateFlow()

    private val _error = MutableSharedFlow<String>()
    val error = _error.asSharedFlow()

    private val _eventSaved = MutableSharedFlow<Unit>()
    val eventSaved = _eventSaved.asSharedFlow()

    init {
        val eventId = savedStateHandle.get<Long>("eventId") ?: 0L
        if (eventId != 0L) loadForEdit(eventId)
    }

    private fun loadForEdit(id: Long) {
        viewModelScope.launch {
            repository.getEventById(id)
                .onSuccess { event ->
                    _state.update {
                        it.copy(
                            eventId = event.id,
                            content = event.content,
                            link = event.link.orEmpty(),
                            dateTimeMillis = runCatching {
                                Instant.parse(event.datetime).toEpochMilli()
                            }.getOrNull(),
                            type = event.type,
                            speakerIds = event.speakerIds,
                            coords = event.coords,
                            attachment = event.attachment
                        )
                    }
                }
                .onFailure { error ->
                    _error.emit(error.message ?: "Не удалось загрузить событие")
                }
        }
    }

    fun updateContent(value: String) = _state.update { it.copy(content = value) }

    fun setDateTime(millis: Long) = _state.update { it.copy(dateTimeMillis = millis) }

    fun setType(type: EventType) = _state.update { it.copy(type = type) }

    fun setSpeakers(ids: List<Long>) = _state.update { it.copy(speakerIds = ids) }

    fun attach(file: File) {
        val type = mediaRepository.mediaTypeOf(file)
        if (type == null) {
            viewModelScope.launch { _error.emit("Неподдерживаемый тип файла") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(uploading = true) }
            mediaRepository.uploadMedia(file)
                .onSuccess { media ->
                    _state.update {
                        it.copy(
                            uploading = false,
                            attachment = AttachmentDto(url = media.url, type = type.name)
                        )
                    }
                }
                .onFailure { error ->
                    _state.update { it.copy(uploading = false) }
                    _error.emit(error.message ?: "Ошибка загрузки")
                }
        }
    }

    fun removeAttachment() = _state.update { it.copy(attachment = null) }

    fun save() {
        val current = _state.value
        if (current.content.isBlank()) {
            viewModelScope.launch {
                _error.emit("Описание события не может быть пустым")
            }
            return
        }
        val millis = current.dateTimeMillis
        if (millis == null) {
            viewModelScope.launch { _error.emit("Укажите дату и время мероприятия") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(saving = true) }
            val request = CreateEventRequest(
                id = current.eventId,
                content = current.content.trim(),
                datetime = Instant.ofEpochMilli(millis).toString(),
                type = current.type,
                speakerIds = current.speakerIds,
                link = current.link.trim().takeIf { it.isNotBlank() },
                coords = current.coords,
                attachment = current.attachment
            )
            repository.createEvent(request)
                .onSuccess {
                    _state.update { it.copy(saving = false) }
                    _eventSaved.emit(Unit)
                }
                .onFailure { error ->
                    _state.update { it.copy(saving = false) }
                    _error.emit(error.message ?: "Не удалось сохранить событие")
                }
        }
    }
}