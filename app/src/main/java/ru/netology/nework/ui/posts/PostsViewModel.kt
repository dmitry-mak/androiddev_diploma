package ru.netology.nework.ui.posts

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
import ru.netology.nework.dto.PostDto
import ru.netology.nework.error.AppError
import ru.netology.nework.error.toAppError
import ru.netology.nework.repository.PostRepository
import javax.inject.Inject


@HiltViewModel
class PostsViewModel @Inject constructor(
    private val repository: PostRepository,
    private val appAuth: AppAuth
) : ViewModel() {
    val pagingPosts: Flow<PagingData<PostDto>> = repository.pagingPosts()
        .map { pagingData ->
            pagingData.map { post ->
                post.copy(ownedByMe = post.authorId == myId)
            }
        }.cachedIn(viewModelScope)


    private val _error = MutableSharedFlow<AppError>()
    val error = _error.asSharedFlow()

    private val myId: Long get() = appAuth.authState.value.id

    fun isAuthorized(): Boolean = appAuth.authState.value.token != null

    fun like(post: PostDto) {

        viewModelScope.launch {
            val optimistic = post.copy(
                likedByMe = !post.likedByMe,
                likeOwnerIds = if (post.likedByMe) post.likeOwnerIds - myId
                else post.likeOwnerIds + myId
            )
            repository.updateDb(optimistic)
            repository.like(post)
                .onSuccess { serverPost ->
                    repository.updateDb(serverPost.copy(ownedByMe = post.ownedByMe))
                }
                .onFailure { error ->
                    repository.updateDb(post)
                    _error.emit(error.toAppError())
                }
        }
    }

    fun delete(post: PostDto) {
        viewModelScope.launch {
            repository.deleteFromDb(post.id)
            repository.deletePost(post.id)
                .onFailure { error ->
                    repository.updateDb(post)
                    _error.emit(error.toAppError())
                }
        }
    }
}