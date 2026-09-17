package ru.netology.nework.ui.posts

import android.net.Uri
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
import ru.netology.nework.dto.CreatePostRequest
import ru.netology.nework.repository.MediaRepository
import ru.netology.nework.repository.PostRepository
import java.io.File
import javax.inject.Inject


data class CreatePostUiState(
    val postId: Long = 0,
    val content: String = "",
    val link: String = "",
    val attachment: AttachmentDto? = null,
    val previewUri: Uri? = null,
    val uploading: Boolean = false,
    val saving: Boolean = false
)

@HiltViewModel
class CreatePostViewModel @Inject constructor(
    private val repository: PostRepository,
    private val mediaRepository: MediaRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(CreatePostUiState())
    val state = _state.asStateFlow()

    private val _error = MutableSharedFlow<String>()
    val error = _error.asSharedFlow()

    private val _postSaved = MutableSharedFlow<Unit>()
    val postSaved = _postSaved.asSharedFlow()

    init {
        val postId = savedStateHandle.get<Long>("postId") ?: 0
        if (postId != 0L) loadForEdit(postId)
    }

    private fun loadForEdit(id: Long) {
        viewModelScope.launch {
            repository.getPostById(id)
                .onSuccess { post ->
                    _state.update {
                        it.copy(
                            postId = post.id,
                            content = post.content,
                            link = post.link.orEmpty()
                        )
                    }
                }
                .onFailure { e ->
                    _error.emit(e.message ?: "Не удалось загрузить пост")
                }
        }
    }

    fun updateContent(value: String) = _state.update {
        it.copy(
            content = value
        )
    }

    fun updateLink(value: String) = _state.update {
        it.copy(
            link = value
        )
    }

    fun attach(file: File) {
        val type = mediaRepository.mediaTypeOf(file)
        if (type == null) {
            viewModelScope.launch {
                _error.emit("Такой тип файла не поддерживается")
            }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(uploading = true) }
            mediaRepository.uploadMedia(file)
                .onSuccess { media ->
                    _state.update {
                        it.copy(
                            uploading = false,
                            attachment = AttachmentDto(
                                url = media.url,
                                type = type.name
                            )
                        )
                    }
                }
                .onFailure { e ->
                    _state.update { it.copy(uploading = false) }
                    _error.emit(e.message ?: "Ошибка загрузки")
                }
        }
    }

    fun removeAttachment() = _state.update {
        it.copy(attachment = null)
    }

    fun save() {
        val current = _state.value
        if (current.content.isBlank()) {
            viewModelScope.launch {
                _error.emit("Содержимое поста не может быть пустым")
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(saving = true) }

            val request = CreatePostRequest(
                id = current.postId,
                content = current.content.trim(),
                link = current.link.trim().takeIf { it.isNotBlank() },
                attachment = current.attachment
            )

            repository.createPost(request)
                .onSuccess {
                    _state.update { it.copy(saving = false) }
                    _postSaved.emit(Unit)
                }
                .onFailure { e ->
                    _state.update { it.copy(saving = false) }
                    _error.emit(e.message ?: "Не удалось сохранить пост")
                }
        }
    }


}