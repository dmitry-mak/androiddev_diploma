package ru.netology.nework.repository

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okio.IOException
import ru.netology.nework.api.ApiService
import ru.netology.nework.dto.AttachmentType
import ru.netology.nework.dto.MediaDto
import java.io.File
import java.net.URLConnection
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class MediaRepository @Inject constructor(
    private val apiService: ApiService,
    @ApplicationContext private val context: Context
) {

    companion object {
        const val FILE_MAX_SIZE = 1024 * 1024 * 15L //не больше 15 мб, по ТЗ
    }

    fun mediaTypeOf(file: File): AttachmentType? {
        val extension = file.extension.lowercase()
        return when {
            extension in listOf("jpg", "jpeg", "png", "gif") -> AttachmentType.IMAGE
            extension in listOf("mp4", "mov", "avi", "mkv") -> AttachmentType.VIDEO
            extension in listOf("mp3", "wav", "aac") -> AttachmentType.AUDIO
            else -> null
        }
    }

    suspend fun uploadMedia(file: File): Result<MediaDto> = withContext(Dispatchers.IO) {
        try {
            if (file.length() > FILE_MAX_SIZE) {
                return@withContext Result.failure(Exception("Размер файла превышает 15 МБ"))
            }

            val mimeType =
                URLConnection.guessContentTypeFromName(file.name) ?: "application/octet-stream"

            val requestBody = file.asRequestBody(mimeType.toMediaType())
            val multiPartBody = MultipartBody.Part.createFormData(
                "file",
                file.name,
                requestBody
            )
            val response = apiService.uploadMedia(multiPartBody)
            if (response.isSuccessful) {
                val body = response.body()
                    ?: return@withContext Result.failure(Exception("Пустой ответ от сервера"))
                Result.success(body)
            } else {
                Result.failure(Exception("Ошибка загрузки: ${response.code()}"))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Ошибка сети"))
        }
    }
}