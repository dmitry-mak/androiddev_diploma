package ru.netology.nework.ui.users

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
import ru.netology.nework.dto.JobDto
import ru.netology.nework.dto.PostDto
import ru.netology.nework.dto.UserDto
import ru.netology.nework.repository.PostRepository
import ru.netology.nework.repository.UserRepository
import ru.netology.nework.repository.WallRepository
import javax.inject.Inject


data class UserProfileUiState(
    val user: UserDto? = null,
    val wall: List<PostDto> = emptyList(),
    val jobs: List<JobDto> = emptyList(),
    val loading: Boolean = false
)

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val wallRepository: WallRepository,
    private val postRepository: PostRepository,
    private val appAuth: AppAuth,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val userId: Long = savedStateHandle.get<Long>("userId") ?: 0L
    val isMyProfile: Boolean = userId == appAuth.authState.value.id && userId != 0L

    private val _state = MutableStateFlow(UserProfileUiState())
    val state = _state.asStateFlow()

    private val _error = MutableSharedFlow<String>()
    val error = _error.asSharedFlow()

    init {
        loadAll()
    }

    fun loadAll() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }

            val userResult = userRepository.getUserById(userId)
            val wallResult =
                if (isMyProfile) wallRepository.getMyWall() else wallRepository.getWall(userId)
            val jobsResult =
                if (isMyProfile) userRepository.getMyJobs() else userRepository.getJobs(userId)

            userResult.onSuccess { user ->
                _state.update { it.copy(user = user) }
            }
                .onFailure { error ->
                    _error.emit(error.message ?: "Не удалось загрузить профиль пользователя")
                }

            wallResult.onSuccess { wall ->
                _state.update { it.copy(wall = wall.sortedByDescending { post -> post.id }) }
            }
                .onFailure { error ->
                    _error.emit(error.message ?: "Не удалось загрузить стену")
                }

            jobsResult.onSuccess { jobs ->
                _state.update { it.copy(jobs = jobs) }
            }
                .onFailure { error ->
                    _error.emit(error.message ?: "Не удалось загрузить список мест работы")
                }
            _state.update { it.copy(loading = false) }
        }
    }

    fun isAuthorized(): Boolean = appAuth.authState.value.token != null

    fun reloadWall() {
        viewModelScope.launch {
            val result =
                if (isMyProfile) wallRepository.getMyWall() else wallRepository.getWall(userId)
            result.onSuccess { wall ->
                _state.update { it.copy(wall = wall.sortedByDescending { post -> post.id }) }
            }
                .onFailure { error ->
                    _error.emit(error.message ?: "Не удалось обновить стену")
                }
        }
    }

    fun like(post: PostDto) {
        val optimistic = post.copy(
            likedByMe = !post.likedByMe,
            likeOwnerIds = if (post.likedByMe) post.likeOwnerIds - appAuth.authState.value.id
            else post.likeOwnerIds + appAuth.authState.value.id
        )
        updateWallPost(optimistic)
        viewModelScope.launch {
            val result = if (isMyProfile) wallRepository.likeMyPost(post) else wallRepository.like(
                userId,
                post
            )
            result.onSuccess { server ->
                updateWallPost(server)
            }.onFailure { error ->
                updateWallPost(post)
                _error.emit(error.message ?: "Не удалось изменить лайк")
            }
        }
    }

    fun deletePost(post: PostDto) {
        viewModelScope.launch {
            postRepository.deletePost(post.id)
                .onSuccess {
                    _state.update {
                        it.copy(wall = it.wall.filterNot { currentPost ->
                            currentPost.id == post.id
                        })
                    }
                }
                .onFailure { error ->
                    _error.emit(error.message ?: "Не удалось удалить пост")
                }
        }
    }

    fun deleteJob(job: JobDto) {
        viewModelScope.launch {
            userRepository.deleteMyJob(job.id)
                .onSuccess {
                    _state.update {
                        it.copy(jobs = it.jobs.filterNot { currentJob ->
                            currentJob.id == job.id
                        })
                    }
                }
                .onFailure { error ->
                    _error.emit(error.message ?: "Не удалось удалить работу")
                }
        }
    }

    private fun updateWallPost(post: PostDto) {
        _state.update {
            it.copy(
                wall = it.wall.map { currentPost ->
                    if (currentPost.id == post.id) post else currentPost
                }
            )
        }
    }

}