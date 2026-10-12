package ru.netology.nework.ui.posts

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.adapter.LoadStateFooterAdapter
import ru.netology.nework.adapter.OnPostInteractionListener
import ru.netology.nework.adapter.PostAdapter
import ru.netology.nework.databinding.FragmentPostsBinding
import ru.netology.nework.dto.PostDto
import ru.netology.nework.error.toUserMessage
import ru.netology.nework.util.UrlUtils
import ru.netology.nework.util.showAuthDialog

@AndroidEntryPoint
class PostsFragment : Fragment(R.layout.fragment_posts) {

    private val viewModel: PostsViewModel by viewModels()
    private var _binding: FragmentPostsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: PostAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentPostsBinding.bind(view)

        setupAdapter()
        setupSwipeRefresh()
        setupFab()
        setupObservers()
        setupPostChangedListener()
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            adapter.refresh()
        }
    }

    private fun setupAdapter() {
        binding.postsList.layoutManager = LinearLayoutManager(requireContext())
        adapter = PostAdapter(object : OnPostInteractionListener {

            override fun onLike(post: PostDto) {
                if (viewModel.isAuthorized()) {
                    viewModel.like(post)
                } else {
                    showAuthDialog()
                }
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
                    R.id.action_postsFragment_to_postDetailFragment,
                    bundle
                )
            }

            override fun onEdit(post: PostDto) {
                val bundle = Bundle().apply { putLong("postId", post.id) }
                findNavController().navigate(
                    R.id.action_postsFragment_to_createPostFragment,
                    bundle
                )
            }

            override fun onRemove(post: PostDto) {
                viewModel.delete(post)
            }

            override fun onImageClick(post: PostDto) {
                //                прописать на шаге № 3 - просмотр полноразмерного фото
                Toast.makeText(requireContext(), "Просмотр вложения - фото", Toast.LENGTH_SHORT)
                    .show()
            }

            override fun onPlayMedia(post: PostDto) {
                val url = post.attachment?.url ?: return
                val mediaUrl = UrlUtils.mediaUrl(url) ?: return
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(mediaUrl))
                startActivity(intent)
            }
        })
        binding.postsList.adapter = adapter.withLoadStateFooter(
            LoadStateFooterAdapter {
                adapter.retry()
            }
        )
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.pagingPosts.collect { pagingData ->
                        adapter.submitData(pagingData)
                    }
                }
                launch {
                    adapter.loadStateFlow.collect { states ->
                        val refresh = states.refresh
                        binding.swipeRefresh.isRefreshing = refresh is LoadState.Loading
                        binding.progress.isVisible =
                            refresh is LoadState.Loading && adapter.itemCount == 0
                        binding.empty.isVisible =
                            refresh is LoadState.NotLoading && adapter.itemCount == 0
                    }
                }
                launch {
                    viewModel.error.collect { error ->
                        Toast.makeText(requireContext(), error.toUserMessage(), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun setupPostChangedListener() {
        findNavController().currentBackStackEntry?.savedStateHandle
            ?.getLiveData<Boolean>(CreatePostFragment.IS_POST_CHANGED)
            ?.observe(viewLifecycleOwner) { postChanged ->
                if (postChanged == true) {
                    adapter.refresh()
                    findNavController().currentBackStackEntry?.savedStateHandle
                        ?.set(CreatePostFragment.IS_POST_CHANGED, false)
                }
            }
    }

    private fun setupFab() {
        binding.fab.setOnClickListener {
            if (viewModel.isAuthorized()) {
                findNavController().navigate(R.id.action_postsFragment_to_createPostFragment)
            } else {
                showAuthDialog()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}