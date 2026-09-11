package ru.netology.nework.auth

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class AppAuth @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)

    private val _authStateFlow = MutableStateFlow(AuthState())

    val authState: StateFlow<AuthState>
        get() = _authStateFlow

    init {
        val id = prefs.getLong(KEY_ID, 0)
        val token = prefs.getString(KEY_TOKEN, null)
//        Раскоментировать после подключения загрузки изображений:
//        val avatar = prefs.getString(KEY_AVATAR, null)

        if (id != 0L && !token.isNullOrEmpty()) {
//            Раскоментировать после подключения загрузки изображений:
//            _authStateFlow.value = AuthState(id, token, avatar)
            _authStateFlow.value = AuthState(id, token)
        }
    }

    fun setAuth(id: Long, token: String) {
        _authStateFlow.value = AuthState(id, token)
        prefs.edit {
            putLong(KEY_ID, id)
            putString(KEY_TOKEN, token)
//            Раскоментировать после подключения загрузки изображений:
//            putString(KEY_AVATAR, avatar)
        }
    }

    fun removeAuth() {
        _authStateFlow.value = AuthState()
        prefs.edit {
            putLong(KEY_ID, 0)
            putString(KEY_TOKEN, null)
        }
    }


    companion object {
        private const val KEY_ID = "id"
        private const val KEY_TOKEN = "token"
        private const val KEY_AVATAR = "avatar"
    }
}

data class AuthState(
    val id: Long = 0L,
    val token: String? = null,
//    Раскоментировать после подключения загрузки изображений:
//    val avatar: String? = null
)