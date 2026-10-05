package ru.netology.nework.ui.users

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.adapter.UsersInfoAdapter
import ru.netology.nework.databinding.FragmentUsersListBinding

@AndroidEntryPoint
class UsersFragment : Fragment(R.layout.fragment_users_list) {

    private val viewModel: UsersViewModel by viewModels()
    private var _binding: FragmentUsersListBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: UsersInfoAdapter
    private var authDialog: AlertDialog? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentUsersListBinding.bind(view)

        setupProfileMenu()
        setupAdapter()
        setupObservers()
    }

    private fun setupProfileMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_users_tab, menu)
            }

            override fun onMenuItemSelected(item: MenuItem): Boolean =
                when (item.itemId) {
                    R.id.menu_my_profile -> {
                        if (viewModel.isAuthorized()) openProfile(viewModel.myId())
                        else showAuthDialog()
                        true
                    }

                    else -> false
                }
        }, viewLifecycleOwner)
    }

    private fun setupAdapter() {
        adapter = UsersInfoAdapter(onUserClick = { user ->
            openProfile(user.id)
        })
        binding.usersList.adapter = adapter
    }

    private fun setupObservers() {
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
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
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

    private fun openProfile(userId: Long) {
        val bundle = Bundle().apply { putLong("userId", userId) }
        findNavController().navigate(R.id.action_usersFragment_to_userProfileFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        authDialog?.dismiss()
        authDialog = null
        _binding = null
    }
}