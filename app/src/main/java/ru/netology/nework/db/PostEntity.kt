package ru.netology.nework.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nework.dto.AttachmentDto
import ru.netology.nework.dto.CoordinatesDto
import ru.netology.nework.dto.PostDto
import ru.netology.nework.dto.UserPreviewDto
import kotlin.Long


@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey val id: Long,
    val authorId: Long,
    val author: String,
    val authorJob: String?,
    val authorAvatar: String?,
    val content: String,
    val published: String,
    val coords: CoordinatesDto?,
    val link: String?,
    val mentionIds: List<Long>?,
    val mentionedMe: Boolean,
    val likeOwnerIds: List<Long>?,
    val likedByMe: Boolean,
    val attachment: AttachmentDto?,
    val users: Map<String, UserPreviewDto>?,
    val ownedByMe: Boolean
)

fun PostDto.toEntity() = PostEntity(
    id = id,
    authorId = authorId,
    author = author,
    authorJob = authorJob,
    authorAvatar = authorAvatar,
    content = content,
    published = published,
    coords = coords,
    link = link,
    mentionIds = mentionIds,
    mentionedMe = mentionedMe,
    likeOwnerIds = likeOwnerIds,
    likedByMe = likedByMe,
    attachment = attachment,
    users = users,
    ownedByMe = ownedByMe
)

fun PostEntity.toDto() = PostDto(
    id = id,
    authorId = authorId,
    author = author,
    authorJob = authorJob,
    authorAvatar = authorAvatar,
    content = content,
    published = published,
    coords = coords,
    link = link,
    mentionIds = mentionIds.orEmpty(),
    mentionedMe = mentionedMe,
    likeOwnerIds = likeOwnerIds.orEmpty(),
    likedByMe = likedByMe,
    attachment = attachment,
    users = users.orEmpty(),
    ownedByMe = ownedByMe
)