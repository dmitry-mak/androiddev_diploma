package ru.netology.nework.ui.posts

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.databinding.AvatarRowBinding
import ru.netology.nework.databinding.FragmentPostDetailBinding
import ru.netology.nework.dto.PostDto
import ru.netology.nework.util.DateUtils
import ru.netology.nework.util.UrlUtils
import kotlin.getValue


@AndroidEntryPoint
class PostDetailFragment : Fragment(R.layout.fragment_post_detail) {
    private val viewmodel: PostDetailViewModel by viewModels()
    private var _binding: FragmentPostDetailBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPostDetailBinding.bind(view)

        setupShareMenu()
        observeState()
    }


    private fun setupShareMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_post_detail, menu)
            }

            override fun onMenuItemSelected(item: MenuItem): Boolean =
                when (item.itemId) {
                    R.id.menu_share -> {
                        viewmodel.state.value.post?.let { sharePost(it) }
                        true
                    }

                    else -> false
                }
        }, viewLifecycleOwner)
    }

    private fun sharePost(post: PostDto) {
        val shareText = buildString {
            append(post.content)
            if (!post.link.isNullOrBlank()) {
                appendLine()
                append(post.link)
            }
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        startActivity(Intent.createChooser(intent, "Поделиться"))
    }


    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewmodel.state.collect { render(it) }
                }
            }
        }
    }

    private fun render(state: PostDetailUiState) {
        binding.progress.isVisible = state.loading
        val post = state.post ?: return

        with(binding) {
            val avatarUrl = UrlUtils.avatarUrl(post.authorAvatar)

            if (avatarUrl != null) {
                authorBasicAvatar.isVisible = false
                authorAvatarImage.isVisible = true
                Glide.with(authorAvatarImage).load(avatarUrl).circleCrop().into(authorAvatarImage)
            } else {
                authorAvatarImage.isVisible = false
                authorBasicAvatar.isVisible = true
                authorBasicAvatar.text = post.author.take(1).uppercase()
            }
            authorName.text = post.author
            authorJob.isVisible = !post.authorJob.isNullOrBlank()
            authorJob.text = post.authorJob.orEmpty()

            when (post.attachment?.type) {
                "IMAGE" -> {
                    attachmentContainer.isVisible = true
                    playIcon.isVisible = false
                    Glide.with(attachmentImage)
                        .load(UrlUtils.mediaUrl(post.attachment.url))
                        .centerCrop()
                        .into(attachmentImage)
                }

                "VIDEO", "AUDIO" -> {
                    attachmentContainer.isVisible = true
                    playIcon.isVisible = true
                    attachmentContainer.setOnClickListener {
                        val url =
                            UrlUtils.mediaUrl(post.attachment.url) ?: return@setOnClickListener
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    }
                }

                else -> attachmentContainer.isVisible = false
            }

            published.text = DateUtils.formatTimeForOutput(post.published)
            postContent.text = post.content

            likedList.isVisible = post.likeOwnerIds.isNotEmpty()
            likedCount.text = post.likeOwnerIds.size.toString()
            fillAvatarRow(
                row = likedRow,
                users = state.likersList.map { AvatarBlock(it.name, it.avatar) }
            ) { navigateToUsers(post.likeOwnerIds.toLongArray(), "Likers") }

            binding.mentionList.isVisible = post.mentionIds.isNotEmpty()
            mentionsCount.text = post.mentionIds.size.toString()
            fillAvatarRow(
                row = mentionedRow,
                users = state.mentioned.map { AvatarBlock(it.name, it.avatar) }
            ) {
                navigateToUsers(post.mentionIds.toLongArray(), "Mentioned")
            }

        }

    }

    private data class AvatarBlock(val name: String, val avatar: String?)

    private fun fillAvatarRow(
        row: LinearLayout,
        users: List<AvatarBlock>,
        onMoreClick: () -> Unit
    ) {
        while (row.childCount > 2) {
            row.removeViewAt(row.childCount - 1)
        }
        fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()
//        val slotSize = 40
//        val slotMargin = 8
        val slotSize = dpToPx(40)
        val slotMargin = dpToPx(8)

        users.take(5).forEach { user ->
            val slot = AvatarRowBinding.inflate(layoutInflater, row, false)
            val url = UrlUtils.avatarUrl(user.avatar)

            if (url != null) {
                slot.avatarInit.isVisible = false
                slot.avatarImage.isVisible = true
                Glide.with(slot.avatarImage).load(url).centerCrop().into(slot.avatarImage)
            } else {
                slot.avatarImage.isVisible = false
                slot.avatarInit.isVisible = true
                slot.avatarInit.text = user.name.take(1).uppercase()
            }
            val linParams =
                LinearLayout.LayoutParams(slotSize, slotSize).apply { marginStart = slotMargin }
            row.addView(slot.root, linParams)
        }

        if (users.size > 5) {
            val more = TextView(requireContext()).apply {
                setBackgroundResource(R.drawable.bg_avatar_circle)
                text = "+"
                gravity = android.view.Gravity.CENTER
                setTextColor(android.graphics.Color.WHITE)
                textSize = 18f
                setOnClickListener { onMoreClick() }
            }
            val linParams =
                LinearLayout.LayoutParams(slotSize, slotSize).apply { marginStart = slotMargin }
            row.addView(more, linParams)
        }
    }

    private fun navigateToUsers(userIds: LongArray, title: String) {
        val bundle = Bundle().apply {
            putLongArray("userIds", userIds)
            putString("title", title)
        }
        findNavController().navigate(R.id.action_postDetailFragment_to_usersListFragment, bundle)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}