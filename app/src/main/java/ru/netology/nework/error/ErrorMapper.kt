package ru.netology.nework.error


fun AppError.toUserMessage(): String = when (this) {
    AppError.Network -> "Ошибка сети"
    is AppError.Server -> "Ошибка сервера: $code"
    AppError.Unauthorized -> "Необходимо авторизоваться"
    AppError.NotFound -> "Запись не найдена"
    AppError.InvalidCredentials -> "Неверный логин или пароль"
    AppError.UserAlreadyExists -> "Пользователь с таким именем уже существует"
    AppError.InvalidImageFormat -> "Неподдерживаемый формат изображения"
    AppError.UnsupportedAttachmentTYpe -> "Неподдерживаемый тип файла"
    AppError.EmptyPostContent -> "Содержимое поста не может быть пустым"
    AppError.EmptyEventContent -> "Добавьте описание мероприятия"
    AppError.DateTimeRequired -> "Укажите дату и время мероприятия"
    AppError.Unknown -> "Неизвестная ошибка"
}