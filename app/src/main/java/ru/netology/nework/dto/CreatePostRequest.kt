package ru.netology.nework.dto

data class CreatePostRequest(
    val id: Long = 0,
    val content: String = "",
    val coords: CoordinatesDto? = null,
    val link: String? = null,
    val mentionIds: List<Long> = emptyList(),
    val attachment: AttachmentDto? = null
)
