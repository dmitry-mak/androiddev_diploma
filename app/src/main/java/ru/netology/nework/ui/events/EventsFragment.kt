package ru.netology.nework.ui.events


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.adapter.EventAdapter
import ru.netology.nework.adapter.LoadStateFooterAdapter
import ru.netology.nework.adapter.OnEventInteractionListener
import ru.netology.nework.databinding.FragmentEventsBinding
import ru.netology.nework.dto.EventDto
import ru.netology.nework.util.UrlUtils

@AndroidEntryPoint
class EventsFragment : Fragment(R.layout.fragment_events) {

    private val viewModel: EventsViewModel by viewModels()

    private var _binding: FragmentEventsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: EventAdapter
    private var authDialog: AlertDialog? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEventsBinding.bind(view)

        setupAdapter()
        setupSwipeRefresh()
        setupFab()
        setupObservers()
        setupEventChangedListener()
    }


    private fun setupAdapter() {
        binding.eventsList.layoutManager = LinearLayoutManager(requireContext())
        adapter = EventAdapter(object : OnEventInteractionListener {

            override fun onLike(event: EventDto) {
                if (viewModel.isAuthorized()) viewModel.like(event) else showAuthDialog()
            }

            override fun onShare(event: EventDto) {
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

            override fun onOpen(event: EventDto) {
                val bundle = Bundle().apply {
                    putLong("eventId", event.id)
                }
                findNavController().navigate(
                    R.id.action_eventsFragment_to_eventDetailFragment, bundle
                )
            }

            override fun onEdit(event: EventDto) {
                val bundle = Bundle().apply {
                    putLong("eventId", event.id)
                }
                findNavController().navigate(
                    R.id.action_eventsFragment_to_createEventFragment,
                    bundle
                )
            }

            override fun onRemove(event: EventDto) {
                viewModel.delete(event)
            }

            override fun onParticipate(event: EventDto) {
                if (viewModel.isAuthorized()) viewModel.participate(event) else showAuthDialog()
            }

            override fun onPlayMedia(event: EventDto) {
                val url = event.attachment?.url ?: return
                val mediaUrl = UrlUtils.mediaUrl(url) ?: return
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(mediaUrl))
                startActivity(intent)
            }
        })
        binding.eventsList.adapter = adapter.withLoadStateFooter(
            LoadStateFooterAdapter {
                adapter.retry()
            }
        )
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.pagingEvents.collect { pagingData ->
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
                    viewModel.error.collect { message ->
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun setupEventChangedListener() {
        findNavController().currentBackStackEntry?.savedStateHandle
            ?.getLiveData<Boolean>(EVENT_CHANGED_KEY)
            ?.observe(viewLifecycleOwner) { changed ->
                if (changed == true) {
                    adapter.refresh()
                    findNavController().currentBackStackEntry?.savedStateHandle
                        ?.set(EVENT_CHANGED_KEY, false)
                }
            }
    }

    private fun setupFab() {
        binding.fab.setOnClickListener {
            if (viewModel.isAuthorized()) {
                findNavController().navigate(R.id.action_eventsFragment_to_createEventFragment)
            } else {
                showAuthDialog()
            }
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
        adapter.refresh()
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

    override fun onDestroyView() {
        super.onDestroyView()
        authDialog?.dismiss()
        authDialog = null
        _binding = null
    }

    companion object {
        const val EVENT_CHANGED_KEY = "eventChanged"
    }
}