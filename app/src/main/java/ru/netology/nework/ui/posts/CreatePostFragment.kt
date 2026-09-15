package ru.netology.nework.ui.posts

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentCreatePostBinding

@AndroidEntryPoint
class CreatePostFragment : Fragment(R.layout.fragment_create_post) {

    private val viewModel: CreatePostViewModel by viewModels()
    private var _binding: FragmentCreatePostBinding? = null
    private val binding get() = _binding!!

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
            Toast.makeText(requireContext(), "Добавление фото - шаг 3.2", Toast.LENGTH_SHORT).show()
        }
        btnAttach.setOnClickListener {
            Toast.makeText(requireContext(), "Добавление файла - шаг 3.2", Toast.LENGTH_SHORT)
                .show()
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
            Toast.makeText(requireContext(), "Удаление вложения - шаг 3.2", Toast.LENGTH_SHORT)
                .show()
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
                        binding.uploadProgress.isVisible = state.saving
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


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val IS_POST_CHANGED = "postChanged"
    }
}