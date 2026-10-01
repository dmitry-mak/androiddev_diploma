package ru.netology.nework.ui.events

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
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
import ru.netology.nework.databinding.FragmentCreateEventBinding
import ru.netology.nework.dto.EventType
import ru.netology.nework.ui.users.UsersListFragment
import ru.netology.nework.util.DateUtils
import ru.netology.nework.util.UrlUtils
import java.io.File
import kotlin.getValue


@AndroidEntryPoint
class CreateEventFragment : Fragment(R.layout.fragment_create_event) {

    private val viewModel: CreateEventViewModel by viewModels()
    private var _binding: FragmentCreateEventBinding? = null
    private val binding get() = _binding!!

    private var cameraUri: Uri? = null

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) cameraUri?.let { uri ->
            uriToFile(uri)?.let(viewModel::attach)
        }
    }

    private val attachLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { uriToFile(it)?.let(viewModel::attach) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCreateEventBinding.bind(view)

        val eventId = arguments?.getLong("eventId", 0L) ?: 0L
        val title = if (eventId != 0L) "Edit event" else "New event"
        (requireActivity() as AppCompatActivity).supportActionBar?.title = title

        setupMenu()
        setupTextWatchers()
        setupButtons()
        setupResultListeners()
        observeState()
    }


    private fun setupMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
                inflater.inflate(R.menu.menu_create_event, menu)
            }

            override fun onMenuItemSelected(item: MenuItem): Boolean =
                when (item.itemId) {
                    R.id.menu_save_event -> {
                        viewModel.save()
                        true
                    }

                    else -> false
                }
        }, viewLifecycleOwner)
    }

    private fun setupButtons() =
        with(binding) {
            btnAddPhoto.setOnClickListener {
                val file =
                    File(requireContext().cacheDir, "photo_${System.currentTimeMillis()}.jpg")
                cameraUri = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.provider",
                    file
                )
                cameraLauncher.launch(cameraUri)
            }
            btnAttach.setOnClickListener { attachLauncher.launch("*/*") }

            btnSpeakers.setOnClickListener {
                val bundle = Bundle().apply {
                    putString("title", "Speakers")
                    putString("mode", "select")
                    putLongArray("userIds", viewModel.state.value.speakerIds.toLongArray())
                }
                findNavController().navigate(
                    R.id.action_createEventFragment_to_usersListFragment,
                    bundle
                )
            }
            btnLocation.setOnClickListener {
                Toast.makeText(requireContext(), "Добавление карт в разработке", Toast.LENGTH_SHORT)
                    .show()
            }

            btnRemoveAttachment.setOnClickListener { viewModel.removeAttachment() }

            btnParams.setOnClickListener { openParamsInput() }
            paramsRow.setOnClickListener { openParamsInput() }
        }

    private fun setupTextWatchers() {
        binding.newEventContent.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(string: Editable?) {
                viewModel.updateContent(string?.toString().orEmpty())
            }

            override fun beforeTextChanged(
                string: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) = Unit

            override fun onTextChanged(
                string: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) = Unit
        })
    }

    private fun setupResultListeners() {
//       Выбор спикеров
        parentFragmentManager.setFragmentResultListener(
            UsersListFragment.SELECT_USERS_REQUEST_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            val ids =
                bundle.getLongArray(UsersListFragment.SELECTED_IDS_KEY)?.toList() ?: emptyList()
            viewModel.setSpeakers(ids)
        }
// Дата и тип события
        parentFragmentManager.setFragmentResultListener(
            EventParamsBottomInput.REQUEST_KEY, viewLifecycleOwner
        ) { _, bundle ->
            viewModel.setDateTime(bundle.getLong(EventParamsBottomInput.KEY_MILLIS))
            viewModel.setType(
                runCatching {
                    EventType.valueOf(
                        bundle.getString(EventParamsBottomInput.KEY_TYPE) ?: EventType.ONLINE.name
                    )
                }.getOrDefault(EventType.ONLINE)
            )
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.state.collect { state ->
                        if (state.eventId != 0L && binding.newEventContent.text.isNullOrEmpty() && state.content.isNotBlank()) {
                            binding.newEventContent.setText(state.content)
                        }
                        binding.progress.isVisible = state.uploading || state.saving

                        val hasAttachment = state.attachment != null
                        binding.newEventAttachmentContainer.isVisible = hasAttachment
                        if (hasAttachment) {
                            Glide.with(binding.newEventAttachmentImage)
                                .load(UrlUtils.mediaUrl(state.attachment?.url))
                                .centerCrop()
                                .into(binding.newEventAttachmentImage)
                        }

                        val millis = state.dateTimeMillis
                        binding.paramsRow.isVisible = millis != null
                        if (millis != null) {
                            val typeTitle =
                                if (state.type == EventType.ONLINE) "Online" else "Offline"
                            binding.paramsRow.text =
                                "${DateUtils.formatMillisForInput(millis)} - $typeTitle"
                        }

                        binding.speakersCount.isVisible = state.speakerIds.isNotEmpty()
                        binding.speakersCount.text = if (state.speakerIds.isEmpty()) ""
                        else "Speakers: ${state.speakerIds.size}"
                    }
                }
                launch {
                    viewModel.error.collect { message ->
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    }
                }
                launch {
                    viewModel.eventSaved.collect {
                        findNavController().previousBackStackEntry?.savedStateHandle?.set(
                            EventsFragment.EVENT_CHANGED_KEY,
                            true
                        )
                        findNavController().navigateUp()
                    }
                }
            }
        }
    }

    private fun openParamsInput() {
        val state = viewModel.state.value
        EventParamsBottomInput
            .newInstance(state.dateTimeMillis ?: -1L, state.type)
            .show(parentFragmentManager, "event_params")
    }

    private fun uriToFile(uri: Uri): File? {
        return try {
            val fileName = "upload_${System.currentTimeMillis()}"
            val extension = requireContext().contentResolver.getType(uri)
                ?.substringAfter('/')?.substringBefore(';') ?: "bin"
            val file = File(requireContext().cacheDir, "$fileName.$extension")
            requireContext().contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
            file
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Не удалось открыть файл", Toast.LENGTH_SHORT).show()
            null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}