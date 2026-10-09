package ru.netology.nework.ui.posts

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
import ru.netology.nework.dto.PostDto
import ru.netology.nework.repository.PostRepository
import javax.inject.Inject


data class PostsUiState(
    val loading: Boolean = false,
    val posts: List<PostDto> = emptyList()
)

@HiltViewModel
class PostsViewModel @Inject constructor(
    private val repository: PostRepository,
    private val appAuth: AppAuth
) : ViewModel() {

    //    private val _state = MutableStateFlow(PostsUiState())
//    val state: StateFlow<PostsUiState> = _state.asStateFlow()
    val pagingPosts: Flow<PagingData<PostDto>> = repository.pagingPosts()
        .map { pagingData ->
            pagingData.map { post ->
                post.copy(ownedByMe = post.authorId == myId)
            }
        }
        .cachedIn(viewModelScope)


    private val _error = MutableSharedFlow<String>()
    val error = _error.asSharedFlow()

    private val myId: Long get() = appAuth.authState.value.id

    fun isAuthorized(): Boolean = appAuth.authState.value.token != null

//    init {
//        loadPosts()
//    }

//    fun loadPosts() {
//        viewModelScope.launch {
//            _state.update {
//                it.copy(loading = true)
//            }
//
//            repository.getPosts()
//                .onSuccess { posts ->
//                    _state.update {
//                        it.copy(
//                            loading = false,
//                            posts = posts
//                                .sortedByDescending { post -> post.id }
//                                .map { post -> post.withOwnership() }
//                        )
//
//                    }
//                }
//                .onFailure { throwable ->
//                    _state.value = _state.value.copy(
//                        loading = false
//                    )
//                    _error.emit(throwable.message ?: "Не удалось загрузить посты")
//                }
//        }
//    }

    fun like(post: PostDto) {

//        updatePostInList(optimistic)

        viewModelScope.launch {
            val optimistic = post.copy(
                likedByMe = !post.likedByMe,
                likeOwnerIds = if (post.likedByMe) post.likeOwnerIds - myId
                else post.likeOwnerIds + myId
            )
            repository.updateDb(optimistic)
            repository.like(post)
                .onSuccess { serverPost ->
//                    updatePostInList(serverPost.withOwnership())
                    repository.updateDb(serverPost.copy(ownedByMe = post.ownedByMe))
                }
                .onFailure { e ->
//                    updatePostInList(post)
                    repository.updateDb(post)
                    _error.emit(e.message ?: "Не удалось изменить лайк")
                }
        }
    }

    fun delete(post: PostDto) {
        viewModelScope.launch {
//            repository.deletePost(post.id)
//                .onSuccess {
//                    _state.update {
//                        it.copy(
//                            posts = it.posts.filterNot { p ->
//                                p.id == post.id
//                            }
//                        )
//                    }
//                }
            repository.deleteFromDb(post.id)
            repository.deletePost(post.id)
                .onFailure { e ->
                    _error.emit(e.message ?: "Не удалось удалить пост")
                }
        }
    }

//    private fun updatePostInList(post: PostDto) {
//        _state.update {
//            it.copy(
//                posts = it.posts.map { p ->
//                    if (p.id == post.id) post else p
//                }
//            )
//        }
//    }
//
//    private fun PostDto.withOwnership() = copy(
//        ownedByMe = authorId == myId
//    )
}