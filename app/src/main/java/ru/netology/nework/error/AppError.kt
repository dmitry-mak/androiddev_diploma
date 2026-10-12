package ru.netology.nework.error


sealed interface AppError {
    data object Network : AppError
    data class Server(val code: Int) : AppError
    data object Unauthorized : AppError
    data object NotFound : AppError
    data object InvalidCredentials : AppError
    data object UserAlreadyExists : AppError
    data object InvalidImageFormat : AppError
    data object UnsupportedAttachmentTYpe : AppError
    data object EmptyPostContent : AppError
    data object EmptyEventContent : AppError
    data object DateTimeRequired : AppError
    data object Unknown : AppError
}

class AppException(val error: AppError) : Exception(error.toString())

fun Throwable.toAppError(): AppError = (this as? AppException)?.error ?: AppError.Unknown