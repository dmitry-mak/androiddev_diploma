package ru.netology.nework.dto

import com.google.gson.annotations.SerializedName

data class CoordinatesDto(
    val lat: Double,
    @SerializedName("long")
    val lng: Double
)
