package ru.netology.nework.ui.auth

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.auth.AuthViewModel
import ru.netology.nework.databinding.FragmentLoginScreenBinding
import ru.netology.nework.util.setupPasswordToggle

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private val viewModel: AuthViewModel by viewModels()
    private var _binding: FragmentLoginScreenBinding? = null
    private val binding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.etPassword.setupPasswordToggle(
            eyeNormal = R.drawable.ic_eye,
            eyeCrossed = R.drawable.ic_eye_crossed
        )

        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                updateButton()
                clearErrors()
            }

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
            }
        }

        binding.etLogin.addTextChangedListener(watcher)
        binding.etPassword.addTextChangedListener(watcher)

        binding.etLogin.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) clearErrors()
        }

        binding.etPassword.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) clearErrors()
        }

        binding.btnLogin.setOnClickListener {
            val login = binding.etLogin.text?.toString().orEmpty().trim()
            val password = binding.etPassword.text?.toString().orEmpty()
            if (validate()) viewModel.login(login = login, password = password)
        }

        binding.registerLink.setOnClickListener {
            // TODO: implement navigation to registration screen
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.loading.collect { isLoading ->
                        if (isLoading) binding.btnLogin.isEnabled = false
                        else updateButton()
                    }
                }
                launch {
                    viewModel.error.collect { message ->
                        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()

                    }
                }
                launch {
                    viewModel.authState.collect { state ->
                        if (state.token != null) findNavController().popBackStack()
                    }
                }
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateButton() {
        val login = binding.etLogin.text?.toString().orEmpty().trim()
        val password = binding.etPassword.text?.toString().orEmpty().trim()
        binding.btnLogin.isEnabled = login.isNotEmpty() && password.isNotEmpty()
    }

    private fun validate(): Boolean {
        val login = binding.etLogin.text?.toString().orEmpty().trim()
        val password = binding.etPassword.text?.toString().orEmpty().trim()
        var valid = true

        if (login.isEmpty()) {
            binding.loginError.visibility = View.VISIBLE
            valid = false
        }
        if (password.isEmpty()) {
            binding.passwordError.visibility = View.VISIBLE
            valid = false
        }
        return valid
    }

    private fun clearErrors() {
        binding.loginError.visibility = View.GONE
        binding.passwordError.visibility = View.GONE
    }
}
