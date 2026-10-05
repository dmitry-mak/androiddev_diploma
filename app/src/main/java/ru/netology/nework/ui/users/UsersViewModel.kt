package ru.netology.nework.ui.users

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
import ru.netology.nework.dto.UserDto
import ru.netology.nework.repository.UserRepository
import javax.inject.Inject


data class UsersUiState(
    val users: List<UserDto> = emptyList(),
    val loading: Boolean = false
)

@HiltViewModel
class UsersViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val appAuth: AppAuth
) : ViewModel() {

    private val _state = MutableStateFlow(UsersUiState())
    val state = _state.asStateFlow()

    private val _error = MutableSharedFlow<String>()
    val error = _error.asSharedFlow()

    fun isAuthorized(): Boolean = appAuth.authState.value.token != null
    fun myId(): Long = appAuth.authState.value.id

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            userRepository.getUsers()
                .onSuccess { users ->
                    _state.update { it.copy(loading = false, users = users) }
                }
                .onFailure { error ->
                    _state.update { it.copy(loading = false) }
                    _error.emit(error.message ?: "Не удалось загрузить пользователей")
                }
        }
    }
}