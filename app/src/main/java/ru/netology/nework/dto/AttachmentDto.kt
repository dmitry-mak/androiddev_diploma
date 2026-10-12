package ru.netology.nework.dto

data class AttachmentDto(
    val url: String,
    val type: AttachmentType
)

enum class AttachmentType{
    IMAGE,
    VIDEO,
    AUDIO
}