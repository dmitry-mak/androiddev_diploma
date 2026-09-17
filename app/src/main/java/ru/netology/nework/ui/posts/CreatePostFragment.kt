package ru.netology.nework.ui.posts

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
import ru.netology.nework.databinding.FragmentCreatePostBinding
import ru.netology.nework.util.UrlUtils
import java.io.File

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
            //            Toast.makeText(requireContext(), "Добавление фото - шаг 3.2", Toast.LENGTH_SHORT).show()
            val file = File(requireContext().cacheDir, "photo_${System.currentTimeMillis()}.jpg")
            cameraUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                file
            )
            cameraLauncher.launch(cameraUri)
        }

        btnAttach.setOnClickListener {
//            Toast.makeText(requireContext(), "Добавление файла - шаг 3.2", Toast.LENGTH_SHORT)
//                .show()
            attachLauncher.launch("*/*")
        }
        btnMention.setOnClickListener {
            Toast.makeText(requireContext(), "Отметить пользователя - шаг 3.2", Toast.LENGTH_SHORT)
                .show()
        }
        btnLocation.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "Добавление локации - после подключения карт",
                Toast.LENGTH_SHORT
            ).show()
        }
        btnRemoveAttachment.setOnClickListener {
//            Toast.makeText(requireContext(), "Удаление вложения - шаг 3.2", Toast.LENGTH_SHORT)
//                .show()
            viewModel.removeAttachment()
        }
    }

    private fun setupTextWatcher() {
        binding.newPostContent.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(newTextContent: Editable?) {
                viewModel.updateContent(newTextContent?.toString().orEmpty())
            }

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) = Unit

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) = Unit
        })
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.state.collect { state ->
                        binding.uploadProgress.isVisible = state.uploading || state.saving

                        val hasAttachment = state.attachment != null
                        binding.newPostAttachmentContainer.isVisible = hasAttachment
                        if(hasAttachment){
                            val imageSource = state.previewUri ?: UrlUtils.mediaUrl(state.attachment?.url)
                            Glide.with(binding.newPostAttachmentImage)
                                .load(imageSource)
                                .centerCrop()
                                .into(binding.newPostAttachmentImage)
                        }
                    }
                }
                launch {
                    viewModel.error.collect { message ->
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val IS_POST_CHANGED = "postChanged"
    }
}