package ru.netology.nework.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nework.dto.AttachmentDto
import ru.netology.nework.dto.CoordinatesDto
import ru.netology.nework.dto.EventDto
import ru.netology.nework.dto.EventType
import ru.netology.nework.dto.UserPreviewDto
import kotlin.Long

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey val id: Long,
    val authorId: Long,
    val author: String,
    val authorJob: String?,
    val authorAvatar: String?,
    val content: String,
    val datetime: String,
    val published: String,
    val coords: CoordinatesDto?,
    val type: EventType,
    val likeOwnerIds: List<Long>?,
    val likedByMe: Boolean,
    val speakerIds: List<Long>?,
    val participantsIds: List<Long>?,
    val participatedByMe: Boolean,
    val attachment: AttachmentDto?,
    val link: String?,
    val users: Map<String, UserPreviewDto>?,
    val ownedByMe: Boolean
)

fun EventDto.toEntity() = EventEntity(
    id = id,
    authorId = authorId,
    author = author,
    authorJob = authorJob,
    authorAvatar = authorAvatar,
    content = content,
    datetime = datetime,
    published = published,
    coords = coords,
    type = type,
    likeOwnerIds = likeOwnerIds,
    likedByMe = likedByMe,
    speakerIds = speakerIds,
    participantsIds = participantsIds,
    participatedByMe = participatedByMe,
    attachment = attachment,
    link = link,
    users = users,
    ownedByMe = ownedByMe
)

fun EventEntity.toDto()= EventDto(
    id = id,
    authorId = authorId,
    author = author,
    authorJob = authorJob,
    authorAvatar = authorAvatar,
    content = content,
    datetime = datetime,
    published = published,
    coords = coords,
    type = type,
    likeOwnerIds = likeOwnerIds.orEmpty(),
    likedByMe = likedByMe,
    speakerIds = speakerIds.orEmpty(),
    participantsIds = participantsIds.orEmpty(),
    participatedByMe = participatedByMe,
    attachment = attachment,
    link = link,
    users = users.orEmpty(),
    ownedByMe = ownedByMe
)