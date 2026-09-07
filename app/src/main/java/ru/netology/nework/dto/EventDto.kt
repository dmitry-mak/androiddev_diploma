package ru.netology.nework.dto

data class EventDto(
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorJob: String? = null,
    val authorAvatar: String? = null,
    val content: String,
    val datetime: String,
    val published: String,

    val coords: CoordinatesDto? = null,

    val type: EventType = EventType.ONLINE,
    val likeOwnerIds: List<Long> = emptyList(),
    val likedByMe: Boolean = false,
    val speakerIds: List<Long> = emptyList(),
    val participantsIds: List<Long> = emptyList(),
    val participatedByMe: Boolean = false,
    val attachment: AttachmentDto? = null,
    val link: String? = null,
    val users: Map<String, UserPreviewDto> = emptyMap()
)

enum class EventType {
    ONLINE,
    OFFLINE
}


