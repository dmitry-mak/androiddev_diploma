package ru.netology.nework.ui.users

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.adapter.UsersInfoAdapter
import ru.netology.nework.databinding.FragmentUsersListBinding
import kotlin.getValue


@AndroidEntryPoint
class UsersListFragment : Fragment(R.layout.fragment_users_list) {

    private val viewModel: UsersListViewModel by viewModels()
    private var _binding: FragmentUsersListBinding? = null
    private val binding get() = _binding!!
    private val adapter = UsersInfoAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentUsersListBinding.bind(view)

        val title = arguments?.getString("title")
        if (!title.isNullOrBlank()) {
            (requireActivity() as AppCompatActivity).supportActionBar?.title = title
        }

        binding.usersList.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.state.collect { state ->
                        binding.progress.isVisible = state.loading
                        adapter.submitList(state.users)
                    }
                }
                launch {
                    viewModel.error.collect { message ->
                        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}