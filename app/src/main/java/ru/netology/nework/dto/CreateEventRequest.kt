package ru.netology.nework.dto

class CreateEventRequest(

    val id: Long = 0,
    val content: String = "",
    val datetime: String? = null,
    val coords: CoordinatesDto? = null,
    val type: EventType = EventType.ONLINE,
    val speakerIds: List<Long> = emptyList(),
    val link: String? = null,
    val attachment: AttachmentDto? = null
)