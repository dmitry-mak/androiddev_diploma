package ru.netology.nework.dto

data class TokenDto(
    val id: Long,
    val token: String,
    val avatar: String? = null
)
