package ru.netology.nework.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.nework.R
import ru.netology.nework.databinding.EventCardBinding
import ru.netology.nework.dto.EventDto
import ru.netology.nework.dto.EventType
import ru.netology.nework.util.DateUtils
import ru.netology.nework.util.UrlUtils


interface OnEventInteractionListener {
    fun onLike(event: EventDto)
    fun onShare(event: EventDto)
    fun onOpen(event: EventDto)
    fun onEdit(event: EventDto)
    fun onRemove(event: EventDto)
    fun onParticipate(event: EventDto)
    fun onPlayMedia(event: EventDto)
}

class EventAdapter(private val onInteractionListener: OnEventInteractionListener) :
    ListAdapter<EventDto, EventViewHolder>(EventDiffCallback()) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): EventViewHolder {
        val binding = EventCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EventViewHolder(binding, onInteractionListener)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(getItem(position))

    }
}

class EventViewHolder(
    private val binding: EventCardBinding,
    private val onInteractionListener: OnEventInteractionListener
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(event: EventDto) = with(binding) {
        author.text = event.author
        published.text = DateUtils.formatTimeShort(event.published)
        eventContent.text = event.content
        likeCount.text = event.likeOwnerIds.size.toString()
        likeButton.setImageResource(
            if (event.likedByMe) R.drawable.ic_like_filled_24 else R.drawable.ic_like_24
        )

        eventType.text = if (event.type == EventType.ONLINE) "Online" else "Offline"
        eventDatetime.text = DateUtils.formatTimeShort(event.datetime)

//        playIcon.isVisible = event.attachment?.type == "VIDEO" || event.attachment?.type == "AUDIO"
//        playIcon.setOnClickListener { onInteractionListener.onPlayMedia(event) }
        when (event.attachment?.type) {
            "IMAGE" -> {
                attachmentContainer.isVisible = true
                playIcon.isVisible = false
                Glide.with(attachmentImage)
                    .load(UrlUtils.mediaUrl(event.attachment.url))
                    .centerCrop()
                    .into(attachmentImage)
                attachmentContainer.setOnClickListener(null)
            }

            "VIDEO", "AUDIO" -> {
                attachmentContainer.isVisible = true
                playIcon.isVisible = true
                attachmentImage.setImageResource(0)
                attachmentImage.setBackgroundColor(
                    ContextCompat.getColor(itemView.context, R.color.post_media_placeholder)
                )
                attachmentContainer.setOnClickListener { onInteractionListener.onPlayMedia(event) }
            }

            else -> {
                attachmentContainer.isVisible = false
                attachmentContainer.setOnClickListener(null)
            }
        }
//        attachmentContainer.setOnClickListener {
//            if (event.attachment?.type == "VIDEO" || event.attachment?.type == "AUDIO") {
//                onInteractionListener.onPlayMedia(event)
//            }
//        }

        val avatarUrl = UrlUtils.avatarUrl(event.authorAvatar)
        if (avatarUrl != null) {
            avatarInit.isVisible = false
            avatarImage.isVisible = true
            Glide.with(avatarImage)
                .load(avatarUrl)
                .circleCrop()
                .placeholder(R.drawable.bg_avatar_circle)
                .into(avatarImage)
        } else {
            avatarImage.isVisible = false
            avatarInit.isVisible = true
            avatarInit.text = event.author.take(1).uppercase()
        }

//        if (event.attachment?.type == "IMAGE") {
//            attachmentContainer.isVisible = true
//            Glide.with(attachmentImage)
//                .load(UrlUtils.mediaUrl(event.attachment.url))
//                .centerCrop()
//                .into(attachmentImage)
//        } else {
//            attachmentContainer.isVisible = false
//        }

        eventLink.isVisible = !event.link.isNullOrBlank()
        eventLink.text = event.link.orEmpty()

        participantsCount.text = event.participantsIds.size.toString()
        val partColor =
            if (event.participatedByMe) Color.parseColor("#6C4CB4") else Color.parseColor("#6E6A7C")
        participateButton.imageTintList = ColorStateList.valueOf(partColor)
        participantsCount.setTextColor(partColor)

        menuButton.isVisible = event.ownedByMe
        menuButton.setOnClickListener {
            PopupMenu(it.context, it).apply {
                inflate(R.menu.options_event)
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.menu_edit -> {
                            onInteractionListener.onEdit(event); true
                        }

                        R.id.menu_remove -> {
                            onInteractionListener.onRemove(event); true
                        }

                        else -> false
                    }
                }
                show()
            }
        }

        likeButton.setOnClickListener { onInteractionListener.onLike(event) }
        shareButton.setOnClickListener { onInteractionListener.onShare(event) }
        participateButton.setOnClickListener { onInteractionListener.onParticipate(event) }
        root.setOnClickListener { onInteractionListener.onOpen(event) }
    }
}

class EventDiffCallback : DiffUtil.ItemCallback<EventDto>() {
    override fun areItemsTheSame(
        oldItem: EventDto,
        newItem: EventDto
    ): Boolean = oldItem.id == newItem.id

    override fun areContentsTheSame(
        oldItem: EventDto,
        newItem: EventDto
    ): Boolean = oldItem == newItem

}