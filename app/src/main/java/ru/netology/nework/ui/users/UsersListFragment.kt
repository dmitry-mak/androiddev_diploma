package ru.netology.nework.ui.users

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
import ru.netology.nework.adapter.UsersInfoAdapter
import ru.netology.nework.databinding.FragmentUsersListBinding
import kotlin.getValue


@AndroidEntryPoint
class UsersListFragment : Fragment(R.layout.fragment_users_list) {

    private val viewModel: UsersListViewModel by viewModels()
    private var _binding: FragmentUsersListBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: UsersInfoAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentUsersListBinding.bind(view)

//        val title = arguments?.getString("title")?.takeIf { it.isNotBlank() }
//        val mode = arguments?.getString("mode") ?: "view"
//        val preselectedIds = arguments?.getLongArray("userIds")?.toSet() ?: emptySet()
        val title = arguments?.getString(ARG_TITLE)?.takeIf { it.isNotBlank() }
        val mode = arguments?.getString(ARG_MODE) ?: "view"
        val preselectedIds = arguments?.getLongArray(ARG_USER_IDS)?.toSet() ?: emptySet()
        val isSelected = mode == "select"

        if (title != null) {
            (requireActivity() as AppCompatActivity).supportActionBar?.title = title
        }

        adapter = UsersInfoAdapter(
            selectMode = isSelected,
            initialSelected = preselectedIds,
            onUserClick = if (isSelected) null else { user ->
                val bundle = Bundle().apply { putLong(UserProfileFragment.ARG_USER_ID, user.id) }
                findNavController().navigate(
                    R.id.action_usersListFragment_to_userProfileFragment, bundle
                )
            })

        binding.usersList.adapter = adapter

        if (isSelected) {
            requireActivity().addMenuProvider(object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.menu_select_users, menu)
                }

                override fun onMenuItemSelected(item: MenuItem): Boolean =
                    when (item.itemId) {
                        R.id.menu_select_users -> {
                            parentFragmentManager.setFragmentResult(
                                SELECT_USERS_REQUEST_KEY,
                                Bundle().apply {
                                    putLongArray(
                                        SELECTED_IDS_KEY,
                                        adapter.getSelectedIds().toLongArray()
                                    )
                                }
                            )
                            findNavController().navigateUp()
                            true
                        }

                        else -> false
                    }
            }, viewLifecycleOwner)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val SELECT_USERS_REQUEST_KEY = "select_users_request"
        const val SELECTED_IDS_KEY = "selected_ids"
        const val ARG_TITLE = "title"
        const val ARG_MODE = "mode"
        const val ARG_USER_IDS = "userIds"
    }
}