package ru.netology.nework.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.netology.nework.dto.AttachmentDto
import ru.netology.nework.dto.CoordinatesDto
import ru.netology.nework.dto.EventType
import ru.netology.nework.dto.UserPreviewDto

class Converters {

    private val gson = Gson()

    @TypeConverter
    fun fromLongList(value: List<Long>?): String? = value?.let { gson.toJson(it) }

    @TypeConverter
    fun toLongList(value: String?): List<Long>? =
        value?.let { gson.fromJson(it, object : TypeToken<List<Long>>() {}.type) }

    @TypeConverter
    fun fromCoords(value: CoordinatesDto?): String? = value?.let { gson.toJson(it) }

    @TypeConverter
    fun toCoords(value: String?): CoordinatesDto? =
        value?.let { gson.fromJson(it, CoordinatesDto::class.java) }

    @TypeConverter
    fun fromAttachment(value: AttachmentDto?): String? = value?.let { gson.toJson(it) }

    @TypeConverter
    fun toAttachment(value: String?): AttachmentDto? =
        value?.let { gson.fromJson(it, AttachmentDto::class.java) }

    @TypeConverter
    fun fromUsers(value: Map<String, UserPreviewDto>?): String? = value.let { gson.toJson(it) }

    @TypeConverter
    fun toUsers(value: String?): Map<String, UserPreviewDto>? =
        value?.let { gson.fromJson(it, object : TypeToken<Map<String, UserPreviewDto>>() {}.type) }

    @TypeConverter
    fun fromEventType(value: EventType?): String? = value?.name

    @TypeConverter
    fun toEventType(value: String?): EventType? = value?.let {
        runCatching {
            EventType.valueOf(it)
        }.getOrNull()
    }

}