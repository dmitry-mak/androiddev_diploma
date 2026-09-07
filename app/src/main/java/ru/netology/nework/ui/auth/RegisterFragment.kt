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
import ru.netology.nework.databinding.FragmentRegisterScreenBinding
import ru.netology.nework.util.setupPasswordToggle


@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private val viewModel: AuthViewModel by viewModels()
    private var _binding: FragmentRegisterScreenBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.etPassword.setupPasswordToggle(
            eyeNormal = R.drawable.ic_eye,
            eyeCrossed = R.drawable.ic_eye_crossed
        )
        binding.etConfirmPassword.setupPasswordToggle(
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
        binding.etName.addTextChangedListener(watcher)
        binding.etPassword.addTextChangedListener(watcher)
        binding.etConfirmPassword.addTextChangedListener(watcher)

        binding.etLogin.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus)
                clearErrors()
        }
        binding.etName.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus)
                clearErrors()
        }
        binding.etPassword.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus)
                clearErrors()
        }
        binding.etConfirmPassword.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus)
                clearErrors()
        }

        binding.btnRegister.setOnClickListener {
            val login = binding.etLogin.text?.toString().orEmpty().trim()
            val name = binding.etName.text?.toString().orEmpty().trim()
            val password = binding.etPassword.text?.toString().orEmpty()
            val confirmPassword = binding.etConfirmPassword.text?.toString().orEmpty()
            if (validate()) {
                viewModel.register(
                    login = login,
                    name = name,
                    password = password,
                    avatarUri = null
                )
            }
        }

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.loading.collect { loading ->
                        if (loading) binding.btnRegister.isEnabled = false
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
                        if (state.token != null) findNavController().navigateUp()
                    }
                }
            }

        }
    }

    private fun validate(): Boolean {
        val login = binding.etLogin.text?.toString().orEmpty().trim()
        val name = binding.etName.text?.toString().orEmpty().trim()
        val pass = binding.etPassword.text?.toString().orEmpty()
        val confirmPass = binding.etConfirmPassword.text?.toString().orEmpty()
        var valid = true

        if (login.isEmpty()) {
            binding.loginError.visibility = View.VISIBLE
            valid = false
        }
        if (name.isEmpty()) {
            binding.nameError.visibility = View.VISIBLE
            valid = false
        }
        if (pass.isEmpty()) {
            binding.passwordError.visibility = View.VISIBLE
            valid = false
        }
        if (confirmPass.isEmpty() || pass != confirmPass) {
            binding.confirmPasswordError.visibility = View.VISIBLE
            valid = false
        }
        return valid
    }

    private fun updateButton() {
        val login = binding.etLogin.text?.toString().orEmpty().trim()
        val name = binding.etName.text?.toString().orEmpty().trim()
        val pass = binding.etPassword.text?.toString().orEmpty()
        val confirmPass = binding.etConfirmPassword.text?.toString().orEmpty()

        binding.btnRegister.isEnabled =
            login.isNotBlank() &&
                    name.isNotBlank() &&
                    pass.isNotBlank() &&
                    confirmPass.isNotBlank() &&
                    pass == confirmPass
    }

    private fun clearErrors() {
        binding.loginError.visibility = View.GONE
        binding.nameError.visibility = View.GONE
        binding.passwordError.visibility = View.GONE
        binding.confirmPasswordError.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}