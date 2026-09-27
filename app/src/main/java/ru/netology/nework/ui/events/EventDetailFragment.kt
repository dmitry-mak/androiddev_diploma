package ru.netology.nework.ui.events

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentEventDetailBinding
import ru.netology.nework.dto.EventDto
import ru.netology.nework.dto.EventType
import ru.netology.nework.ui.common.AvatarItem
import ru.netology.nework.ui.common.bindAvatarRow
import ru.netology.nework.util.DateUtils
import ru.netology.nework.util.UrlUtils

@AndroidEntryPoint
class EventDetailFragment : Fragment(R.layout.fragment_event_detail) {

    private val viewModel: EventDetailViewModel by viewModels()
    private var _binding: FragmentEventDetailBinding? = null
    private val binding get() = _binding!!
    private var authDialog: AlertDialog? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEventDetailBinding.bind(view)

        setupShareMenu()
        observeState()
    }

    private fun setupShareMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
                inflater.inflate(R.menu.menu_event_detail, menu)
            }

            override fun onMenuItemSelected(item: MenuItem): Boolean =
                when (item.itemId) {
                    R.id.menu_share -> {
                        viewModel.state.value.event?.let { shareEvent(it) }
                        true
                    }

                    else -> false
                }
        }, viewLifecycleOwner)
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.state.collect { render(it) }
                }
                launch {
                    viewModel.error.collect { message ->
                        Toast.makeText(requireContext(),message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun render(state: EventDetailUiState) {
        binding.progress.isVisible = state.loading
        val event = state.event ?: return

        with(binding) {
            val avatarUrl = UrlUtils.avatarUrl(event.authorAvatar)

            if (avatarUrl != null) {
                authorBasicAvatar.isVisible = false
                authorAvatarImage.isVisible = true
                Glide.with(authorAvatarImage).load(avatarUrl).circleCrop().into(authorAvatarImage)
            } else {
                authorAvatarImage.isVisible = false
                authorBasicAvatar.isVisible = true
                authorBasicAvatar.text = event.author.take(1).uppercase()
            }
            authorName.text = event.author
            authorJob.isVisible = !event.authorJob.isNullOrBlank()
            authorJob.text = event.authorJob.orEmpty()

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
                        ContextCompat.getColor(requireContext(), R.color.post_media_placeholder)
                    )
                    attachmentContainer.setOnClickListener {
                        val url =
                            UrlUtils.mediaUrl(event.attachment.url) ?: return@setOnClickListener
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    }
                }

                else -> attachmentContainer.isVisible = false
            }

            eventType.text = if (event.type == EventType.ONLINE) "Online" else "Offline"
            eventDatetime.text = DateUtils.formatTimeShort(event.datetime)
            eventContent.text = event.content

            speakersSection.isVisible = state.speakers.isNotEmpty()
            bindAvatarRow(
                row = speakersRow,
                users = state.speakers.map { AvatarItem(it.name, it.avatar) },
                keepChildren = 0
            ) { navigateToUsers(event.speakerIds.toLongArray(), "Speakers") }

            likedSection.isVisible = event.likeOwnerIds.isNotEmpty()
            likeCount.text = event.likeOwnerIds.size.toString()
            bindAvatarRow(
                row = likersRow,
                users = state.likers.map { AvatarItem(it.name, it.avatar) }
            ) {
                navigateToUsers(event.likeOwnerIds.toLongArray(), "Likers")
            }

            participantsCount.text = event.participantsIds.size.toString()
            val partColor = if (event.participatedByMe) Color.parseColor("#6C4CB4")
            else Color.parseColor("#6E6A7C")
            participantIcon.imageTintList = ColorStateList.valueOf(partColor)
            participantsCount.setTextColor(partColor)

            val participateClick = View.OnClickListener {
                if (viewModel.isAuthorized()) {
                    viewModel.joinEvent()
                    findNavController().previousBackStackEntry?.savedStateHandle?.set(
                        EventsFragment.EVENT_CHANGED_KEY,
                        true
                    )
                } else {
                    showAuthDialog()
                }
            }
            participantIcon.setOnClickListener(participateClick)
            participantsCount.setOnClickListener(participateClick)

            bindAvatarRow(
                row = participantsRow,
                users = state.participants.map { AvatarItem(it.name, it.avatar) }
            ) {
                navigateToUsers(event.participantsIds.toLongArray(), "Participants")
            }
            mapPlaceholder.isVisible = event.coords != null
        }
    }

    private fun navigateToUsers(userIds: LongArray, title: String) {
        val bundle = Bundle().apply {
            putLongArray("userIds", userIds)
            putString("title", title)
        }
        findNavController().navigate(R.id.action_eventDetailFragment_to_usersListFragment, bundle)
    }

    private fun showAuthDialog() {
        if (authDialog?.isShowing == true) return
        authDialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Необходимо авторизоваться")
            .setMessage("Войдите или зарегистрируйтесь чтобы продолжить")
            .setPositiveButton("Вход") { _, _ ->
                findNavController().navigate(R.id.action_global_loginFragment)
            }
            .setNegativeButton("Регистрация") { _, _ ->
                findNavController().navigate(R.id.action_global_registerFragment)
            }
            .setNeutralButton("Отмена", null)
            .show()
    }

    private fun shareEvent(event: EventDto) {
        val shareText = buildString {
            append(event.content)
            if (!event.link.isNullOrBlank()) {
                appendLine()
                append(event.link)
            }
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        startActivity(Intent.createChooser(intent, "Поделиться"))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        authDialog?.dismiss()
        authDialog = null
        _binding = null
    }
}