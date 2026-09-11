package ru.netology.nework.adapter

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
import ru.netology.nework.databinding.PostCardBinding
import ru.netology.nework.dto.PostDto
import ru.netology.nework.util.DateUtils
import ru.netology.nework.util.UrlUtils


interface OnPostInteractionListener {
    fun onLike(post: PostDto)
    fun onShare(post: PostDto)
    fun onOpen(post: PostDto)
    fun onEdit(post: PostDto)
    fun onRemove(post: PostDto)
    fun onImageClick(post: PostDto)
    fun onPlayMedia(post: PostDto)
}

class PostAdapter(
    private val onInteractionListener: OnPostInteractionListener
) : ListAdapter<PostDto, PostViewHolder>(PostDiffCallback()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PostViewHolder {
        val binding = PostCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PostViewHolder(
            binding, onInteractionListener
        )
    }

    override fun onBindViewHolder(holder: PostViewHolder, possition: Int) {
        holder.bind(getItem(possition))
    }
}

class PostViewHolder(
    private val binding: PostCardBinding,
    private val onInteractionListener: OnPostInteractionListener
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(post: PostDto) = with(binding) {
        author.text = post.author
        published.text = DateUtils.formatTimeForOutput(post.published)
        postContent.text = post.content
        likeCount.text = post.likeOwnerIds.size.toString()
        likeButton.setImageResource(
            if (post.likedByMe) R.drawable.ic_like_filled_24 else R.drawable.ic_like_24
        )
        val avatarUrl = UrlUtils.avatarUrl(post.authorAvatar)
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
            avatarInit.text = post.author.take(1).uppercase()

        }

        when (post.attachment?.type) {
            "IMAGE" -> {
                attachmentContainer.isVisible = true
                playIcon.isVisible = false
                Glide.with(attachmentImage)
                    .load(UrlUtils.mediaUrl(post.attachment.url))
                    .centerCrop()
                    .into(attachmentImage)
                attachmentImage.setOnClickListener {
                    onInteractionListener.onImageClick(post)
                }
            }

            "VIDEO", "AUDIO" -> {
                attachmentContainer.isVisible = true
                playIcon.isVisible = true
                attachmentImage.setImageResource(0)
                attachmentImage.setBackgroundColor(
                    ContextCompat.getColor(itemView.context, R.color.post_media_placeholder)
                )
                attachmentContainer.setOnClickListener { onInteractionListener.onPlayMedia(post) }
            }

            else -> {
                attachmentContainer.isVisible = false
                attachmentContainer.setOnClickListener(null)
            }
        }

        postLink.isVisible = !post.link.isNullOrBlank()
        postLink.text = post.link.orEmpty()

        menuButton.isVisible = post.ownedByMe
        menuButton.setOnClickListener {
            PopupMenu(it.context, it).apply {
                inflate(R.menu.options_post)
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.menu_edit -> {
                            onInteractionListener.onEdit(post); true
                        }

                        R.id.menu_remove -> {
                            onInteractionListener.onRemove(post); true
                        }

                        else -> false
                    }
                }
                show()
            }
        }
        likeButton.setOnClickListener { onInteractionListener.onLike(post) }
        shareButton.setOnClickListener { onInteractionListener.onShare(post) }
        root.setOnClickListener { onInteractionListener.onOpen(post) }
    }
}

class PostDiffCallback : DiffUtil.ItemCallback<PostDto>() {
    override fun areItemsTheSame(
        oldItem: PostDto,
        newItem: PostDto
    ): Boolean {
        if (oldItem::class != newItem::class) {
            return false
        }
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: PostDto,
        newItem: PostDto
    ): Boolean {

        return oldItem == newItem
    }
}