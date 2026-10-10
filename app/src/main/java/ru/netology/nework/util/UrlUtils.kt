package ru.netology.nework.util

import ru.netology.nework.BuildConfig

object UrlUtils {

    fun avatarUrl(path: String?): String?=
        path?.takeIf {
            it.isNotBlank()
        }?.let {
            if(it.startsWith("http")) it else "${BuildConfig.BASE_URL}avatars/${it.removePrefix("/")}"
        }

    fun mediaUrl(path: String?): String?=
        path?.takeIf { it.isNotBlank() }?.let {
            if(it.startsWith("http")) it else "${BuildConfig.BASE_URL}${it.removePrefix("/")}"
        }

    fun resizeTransform(url: String?, transform: String): String?=
        url?.let {
            if(it.contains("?")) "$it&tr=$transform" else "$it?tr=$transform"
        }
}