package ru.netology.nework.dto


data class PostDto(
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String? = null,
    val authorJob: String? = null,
    val content: String,
    val published: String,
    val coords: CoordinatesDto? = null,
    val link: String? = null,
    val mentionIds: List<Long> = emptyList(),
    val mentionedMe: Boolean = false,
    val likeOwnerIds: List<Long> = emptyList(),
    val likedByMe: Boolean = false,
    val attachment: AttachmentDto? = null,
    val users: Map<String, UserPreviewDto> = emptyMap(),
    val ownedByMe: Boolean = false
)