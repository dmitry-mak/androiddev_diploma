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
import ru.netology.nework.dto.UserDto
import ru.netology.nework.repository.UserRepository
import javax.inject.Inject


data class UsersListUiState(
    val users: List<UserDto> = emptyList(),
    val loading: Boolean = false
)

@HiltViewModel
class UsersListViewModel @Inject constructor(
    private val userRepository: UserRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val ids: List<Long> =
        (savedStateHandle.get<LongArray>("userIds") ?: LongArray(0)).toList()

    private val _state = MutableStateFlow(UsersListUiState())
    val state = _state.asStateFlow()

    private val _error = MutableSharedFlow<String>()
    val error = _error.asSharedFlow()

    init {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            userRepository.getUsers()
                .onSuccess { users ->
                    val byId = users.associateBy { it.id }
                    _state.update { it.copy(loading = false,users=ids.mapNotNull { byId[it] }) }
                }
                .onFailure { error ->
                    _state.update { it.copy(loading = false) }
                    _error.emit(error.message ?: "Не удалось загрузить список пользователей")
                }
        }

    }

}