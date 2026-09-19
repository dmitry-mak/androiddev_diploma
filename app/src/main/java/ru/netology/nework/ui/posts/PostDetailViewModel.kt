package ru.netology.nework.ui.posts

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
import ru.netology.nework.dto.PostDto
import ru.netology.nework.dto.UserDto
import ru.netology.nework.dto.UserPreviewDto
import ru.netology.nework.repository.PostRepository
import ru.netology.nework.repository.UserRepository
import javax.inject.Inject


data class PostDetailUiState(
    val post: PostDto? = null,
    val likersList: List<UserDto> = emptyList(),
    val mentioned: List<UserPreviewDto> = emptyList(),
    val loading: Boolean = false
)

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val postId: Long = savedStateHandle.get<Long>("postId") ?: 0L

    private val _state = MutableStateFlow(PostDetailUiState())
    val state = _state.asStateFlow()

    private val _error = MutableSharedFlow<String>()
    val error = _error.asSharedFlow()

    init {
        load()
    }

    private fun load(){
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            val  postResult =  postRepository.getPostById(postId)
            val usersResult = userRepository.getUsers()

            postResult
                .onSuccess { post ->
                    val usersById= usersResult.getOrNull()?.associateBy { it.id } ?: emptyMap()
                    val likersList = post.likeOwnerIds.mapNotNull { usersById[it] }
                    val mentioned = post.mentionIds.mapNotNull { id ->
                        post.users[id.toString()] ?: usersById[id]?.let { UserPreviewDto(it.name, it.avatar) }
                    }
                    _state.update {
                        it.copy(
                            loading = false,
                            post = post,
                            likersList = likersList,
                            mentioned = mentioned)
                    }
                }
                .onFailure {  error ->
                    _state.update { it.copy(loading = false)}
                    _error.emit(error.message?: "Не удалось загрузить пост")
                }
        }
    }
}