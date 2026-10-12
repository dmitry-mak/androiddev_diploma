package ru.netology.nework.ui.posts

import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
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
import ru.netology.nework.databinding.FragmentCreatePostBinding
import ru.netology.nework.dto.CoordinatesDto
import ru.netology.nework.error.toUserMessage
import ru.netology.nework.ui.maps.MapPickerFragment
import ru.netology.nework.ui.users.UsersListFragment
import ru.netology.nework.util.UrlUtils
import java.io.File
import java.util.Locale

@AndroidEntryPoint
class CreatePostFragment : Fragment(R.layout.fragment_create_post) {

    private val viewModel: CreatePostViewModel by viewModels()
    private var _binding: FragmentCreatePostBinding? = null
    private val binding get() = _binding!!

    private var cameraUri: Uri? = null

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) cameraUri?.let { uri ->
            val file = uriToFile(uri)
            if (file != null) viewModel.attach(file)
        }
    }

    private val attachLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val file = uriToFile(uri)
            if (file != null) viewModel.attach(file)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentCreatePostBinding.bind(view)

        setupMenu()
        setupTextWatcher()
        setupButtons()
        observeState()

        parentFragmentManager.setFragmentResultListener(
            UsersListFragment.SELECT_USERS_REQUEST_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            val ids = bundle.getLongArray(UsersListFragment.SELECTED_IDS_KEY)
                ?.toList()
                ?: emptyList()
            viewModel.setMentions(ids)
        }
        parentFragmentManager.setFragmentResultListener(
            MapPickerFragment.REQUEST_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            viewModel.setCoords(
                CoordinatesDto(
                    lat = bundle.getDouble(MapPickerFragment.KEY_LAT),
                    lng = bundle.getDouble(MapPickerFragment.KEY_LONG)
                )
            )
        }
    }

    private fun setupMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_create_post, menu)
            }

            override fun onMenuItemSelected(item: MenuItem): Boolean =
                when (item.itemId) {
                    R.id.menu_save_post -> {
                        viewModel.save()
                        true
                    }

                    else -> false
                }
        }, viewLifecycleOwner)
    }

    private fun setupButtons() = with(binding) {
        btnAddPhoto.setOnClickListener {
            val file = File(requireContext().cacheDir, "photo_${System.currentTimeMillis()}.jpg")
            cameraUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                file
            )
            cameraLauncher.launch(cameraUri)
        }

        btnAttach.setOnClickListener {
            attachLauncher.launch("*/*")
        }
        btnMention.setOnClickListener {
            val bundle = Bundle().apply {
//                putString("title", "Tag user")
//                putString("mode", "select")
//                putLongArray("userIds", viewModel.state.value.mentionIds.toLongArray())
                putString(UsersListFragment.ARG_TITLE, "Tag user")
                putString(UsersListFragment.ARG_MODE, "select")
                putLongArray(
                    UsersListFragment.ARG_USER_IDS,
                    viewModel.state.value.mentionIds.toLongArray()
                )
            }
            findNavController().navigate(
                R.id.action_createPostFragment_to_usersListFragment,
                bundle
            )
        }

        btnLocation.setOnClickListener { openCoordsPicker() }
        coordsRow.setOnClickListener { openCoordsPicker() }
        btnRemoveAttachment.setOnClickListener { viewModel.removeAttachment() }
    }

    private fun setupTextWatcher() {
        binding.newPostContent.doAfterTextChanged { newText ->
            viewModel.updateContent(newText?.toString().orEmpty())
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.state.collect { state ->

                        if (state.postId != 0L && binding.newPostContent.text.isNullOrEmpty() && state.content.isNotBlank()) {
                            binding.newPostContent.setText(state.content)
                        }

                        binding.uploadProgress.isVisible = state.uploading || state.saving

                        val hasAttachment = state.attachment != null
                        binding.newPostAttachmentContainer.isVisible = hasAttachment
                        if (hasAttachment) {
                            val imageSource =
                                state.previewUri ?: UrlUtils.mediaUrl(state.attachment?.url)
                            Glide.with(binding.newPostAttachmentImage)
                                .load(imageSource)
                                .centerCrop()
                                .into(binding.newPostAttachmentImage)
                        }

                        binding.mentionCounter.text = if (state.mentionIds.isEmpty()) ""
                        else "Mentioned: ${state.mentionIds.size}"
                        binding.mentionCounter.isVisible = state.mentionIds.isNotEmpty()

                        val coords = state.coords
                        binding.coordsRow.isVisible = coords != null
                        if (coords != null) {
                            binding.coordsRow.text = String.format(
                                Locale.getDefault(),
                                "%.4f, %.4f",
                                coords.lat,
                                coords.lng
                            )
                        }
                    }
                }
                launch {
                    viewModel.error.collect { error ->
                        Toast.makeText(requireContext(), error.toUserMessage(), Toast.LENGTH_SHORT)
                            .show()
                    }
                }
                launch {
                    viewModel.postSaved.collect {
                        findNavController().previousBackStackEntry?.savedStateHandle?.set(
                            IS_POST_CHANGED,
                            true
                        )
                        findNavController().navigateUp()
                    }
                }
            }
        }
    }


    private fun uriToFile(uri: Uri): File? {
        return try {
            val fileName = "upload_${System.currentTimeMillis()}"
            val extension = requireContext().contentResolver.getType(uri)?.substringAfter('/')
                ?.substringBefore(';') ?: "bin"
            val file = File(requireContext().cacheDir, "$fileName.$extension")

            requireContext().contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            file
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Не удалось открыть файл", Toast.LENGTH_SHORT).show()
            null
        }
    }

    private fun openCoordsPicker() {
        val coords = viewModel.state.value.coords
        val bundle = Bundle().apply {
//            putBoolean("hasCoords", coords != null)
//            putDouble("arg_lat", coords?.lat ?: 0.0)
//            putDouble("arg_lng", coords?.lng ?: 0.0)
            putBoolean(MapPickerFragment.ARG_HAS_COORDS, coords != null)
            putDouble(MapPickerFragment.ARG_LAT, coords?.lat ?: 0.0)
            putDouble(MapPickerFragment.ARG_LONG, coords?.lng ?: 0.0)
        }
        findNavController().navigate(R.id.action_createPostFragment_to_mapPickerFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val IS_POST_CHANGED = "postChanged"
        const val ARG_POST_ID = "postId"
    }
}