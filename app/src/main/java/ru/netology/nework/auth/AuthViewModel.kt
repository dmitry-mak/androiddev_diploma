package ru.netology.nework.auth

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.netology.nework.repository.AuthRepository
import javax.inject.Inject


@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository,
    appAuth: AppAuth
) : ViewModel() {
    val authState = appAuth.authState

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _error = MutableSharedFlow<String>()
    val error = _error.asSharedFlow()

    fun login(login: String, password: String) {
        viewModelScope.launch {
            _loading.value = true
            repository.login(login, password)
                .onFailure {
                    _error.emit(it.message ?: "Ошибка")
                }
            _loading.value = false
        }
    }

    fun register(login: String, name: String, password: String, avatarUri: Uri?) {
        viewModelScope.launch {
            _loading.value = true
            repository.register(login, password, name, avatarUri)
                .onFailure {
                    _error.emit(it.message ?: "Ошибка")
                }
            _loading.value = false
        }
    }
}