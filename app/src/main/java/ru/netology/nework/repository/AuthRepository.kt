package ru.netology.nework.repository

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.nework.api.ApiService
import ru.netology.nework.auth.AppAuth
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val appAuth: AppAuth,
    @ApplicationContext private val context: Context
) {

    suspend fun login(login: String, password: String): Result<Unit> = try {
        val response = apiService.login(login, password)
        if (response.isSuccessful) {
            val responseBody =
                response.body() ?: return Result.failure(AuthException("Empty response body"))
            appAuth.setAuth(responseBody.id, responseBody.token)
            Result.success(Unit)
        } else {
            Result.failure(AuthException(loginErrorMessage(response.code())))
        }
    } catch (e: IOException) {
        Result.failure(AuthException("Ошибка сети"))
    } catch (e: Exception) {
        Result.failure(AuthException(e.message ?: "Неизвестная ошибка"))
    }

    suspend fun register(
        login: String,
        password: String,
        name: String,
        avatar: Uri? = null
    ): Result<Unit> = try {
        val response = apiService.register(
            login = login.toTextBody(),
            pass = password.toTextBody(),
            name = name.toTextBody(),
            avatar = avatar?.toMultipartPart()
        )
        if (response.isSuccessful) {
            val responseBody = response.body()
                ?: return Result.failure(AuthException("Пустой ответ от сервера"))
            appAuth.setAuth(responseBody.id, responseBody.token)
            Result.success(Unit)
        } else {
            Result.failure(AuthException(registerErrorMessage(response.code())))
        }
    } catch (e: IOException) {
        Result.failure(AuthException("Ошибка сети"))
    } catch (e: Exception) {
        Result.failure(AuthException(e.message ?: "Неизвестная ошибка"))
    }


    private fun loginErrorMessage(code: Int): String = when (code) {
        400 -> "Неверный логин или пароль"
        404 -> "Неверный логин или пароль"
        else -> "Ошибка сервера: $code"
    }

    private fun registerErrorMessage(code: Int): String = when (code) {
        400, 403 -> "Пользователь с таким логином уже существует"
        415 -> "Невалидный формат фото"
        else -> "Ошибка сервера: $code"
    }

    private fun String.toTextBody() =
        toRequestBody("text/plain".toMediaTypeOrNull())


    private fun Uri.toMultipartPart(): MultipartBody.Part? {
        val resolver = context.contentResolver
        val mime =resolver.getType(this) ?: "image/jpg"
        val bytes = resolver.openInputStream(this)?.use {
            it.readBytes()
        } ?: return null

        return MultipartBody.Part.createFormData(
            "file",
            "avatar.jpg",
            bytes.toRequestBody(mime.toMediaTypeOrNull())
        )
    }
}

class AuthException(message: String) : Exception(message)