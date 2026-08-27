package ru.netology.nework.dto

data class AttachmentDto(
    val url: String,
    val type: String
)

enum class AttachmentType{
    IMAGE,
    VIDEO,
    AUDIO
}