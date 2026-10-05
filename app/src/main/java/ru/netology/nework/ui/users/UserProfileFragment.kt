package ru.netology.nework.ui.users

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.adapter.JobAdapter
import ru.netology.nework.adapter.OnPostInteractionListener
import ru.netology.nework.adapter.PostAdapter
import ru.netology.nework.databinding.FragmentUserProfileBinding
import ru.netology.nework.dto.PostDto
import ru.netology.nework.util.UrlUtils
import kotlin.getValue


@AndroidEntryPoint
class UserProfileFragment : Fragment(R.layout.fragment_user_profile) {

    private val viewModel: UserProfileViewModel by viewModels()

    private var _binding: FragmentUserProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var postAdapter: PostAdapter
    private lateinit var jobAdapter: JobAdapter

    private var authDialog: AlertDialog? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentUserProfileBinding.bind(view)

        setupTabs()
        setupAdapters()
        setupFab()
        setupObservers()
        setupChangedListeners()
    }


    private fun setupTabs() {
        binding.profileTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val isJobs = tab?.position == 1
                binding.jobsPage.isVisible = isJobs
                binding.wallPage.isVisible = !isJobs
                updateFabVisibility(isJobs)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) = Unit

            override fun onTabReselected(tab: TabLayout.Tab?) = Unit
        })
    }

    private fun setupAdapters() {
        binding.wallList.layoutManager = LinearLayoutManager(requireContext())
        postAdapter = PostAdapter(object : OnPostInteractionListener {

            override fun onLike(post: PostDto) {
                if (viewModel.isAuthorized()) viewModel.like(post) else showAuthDialog()
            }

            override fun onShare(post: PostDto) {
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

            override fun onOpen(post: PostDto) {
                val bundle = Bundle().apply { putLong("postId", post.id) }
                findNavController().navigate(
                    R.id.action_userProfileFragment_to_postDetailFragment, bundle
                )
            }

            override fun onEdit(post: PostDto) {
                val bundle = Bundle().apply { putLong("postId", post.id) }
                findNavController().navigate(
                    R.id.action_userProfileFragment_to_createPostFragment, bundle
                )
            }

            override fun onRemove(post: PostDto) {
                viewModel.deletePost(post)
            }

            override fun onImageClick(post: PostDto) {
                Toast.makeText(requireContext(), "Просмотр вложения. Фото", Toast.LENGTH_SHORT)
                    .show()
            }

            override fun onPlayMedia(post: PostDto) {
                val url = post.attachment?.url ?: return
                val mediaUrl = UrlUtils.mediaUrl(url) ?: return
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(mediaUrl)))
            }
        })
        binding.wallList.adapter = postAdapter

        binding.jobsList.layoutManager = LinearLayoutManager(requireContext())
        jobAdapter = JobAdapter(canDelete = viewModel.isMyProfile) { job ->
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Удалить работу из списка")
                .setMessage(job.name)
                .setPositiveButton("Удалить") { _, _ -> viewModel.deleteJob(job) }
                .setNegativeButton("Отмена", null)
                .show()
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.state.collect { state ->
                        binding.progress.isVisible = state.loading

                        state.user?.let { user ->
                            (requireActivity() as AppCompatActivity).supportActionBar?.title =
                                if (viewModel.isMyProfile) "My profile"
                                else "${user.name}/${user.login}"

                            val avatarUrl = UrlUtils.avatarUrl(user.avatar)
                            if (avatarUrl != null) {
                                binding.profileAvatarInit.isVisible = false
                                binding.profileAvatarImage.isVisible = true
                                Glide.with(binding.profileAvatarImage)
                                    .load(avatarUrl)
                                    .centerCrop()
                                    .into(binding.profileAvatarImage)
                            } else {
                                binding.profileAvatarImage.isVisible = false
                                binding.profileAvatarInit.isVisible = true
                                binding.profileAvatarInit.text = user.name.take(1).uppercase()
                            }
                        }
                        binding.walEmpty.isVisible = !state.loading && state.wall.isEmpty()
                        postAdapter.submitList(state.wall)

                        binding.jobsEmpty.isVisible = !state.loading && state.jobs.isEmpty()
                        jobAdapter.submitList(state.jobs)
                    }
                }
                launch {
                    viewModel.error.collect { message ->
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun setupChangedListeners() {
        findNavController().currentBackStackEntry?.savedStateHandle
            ?.getLiveData<Boolean>(POST_CHANGED_KEY)
            ?.observe(viewLifecycleOwner) { changed ->
                if (changed == true) {
                    viewModel.reloadWall()
                    findNavController().currentBackStackEntry?.savedStateHandle?.set(
                        POST_CHANGED_KEY,
                        false
                    )
                }

            }
    }

    private fun setupFab() {
        binding.fabAddJob.setOnClickListener {
            Toast.makeText(requireContext(), "Добавление работы - шаг 5.2", Toast.LENGTH_SHORT)
                .show()
        }
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

    private fun updateFabVisibility(isJobsTab: Boolean) {
        binding.fabAddJob.isVisible = viewModel.isMyProfile && isJobsTab
    }

    companion object {
        const val POST_CHANGED_KEY = "postChanged"
        const val JOB_CHANGED_KEY = "jobChanged"
    }
}