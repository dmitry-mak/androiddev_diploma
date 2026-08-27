package ru.netology.nework.dto

data class UserDto(
    val id: Long,
    val login: String,
    val name: String,
    val avatar: String? = null
)

