package ru.netology.nework.util

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ru.netology.nework.R


fun Fragment.showAuthDialog() {
    MaterialAlertDialogBuilder(requireContext())
        .setTitle(R.string.dialog_authorization_needed)
        .setMessage(R.string.dialog_authorize_to_continue)
        .setPositiveButton(R.string.enter) { _, _ ->
            findNavController().navigate(R.id.action_global_loginFragment)
        }
        .setNegativeButton(R.string.registration_ru) { _, _ ->
            findNavController().navigate(R.id.action_global_registerFragment)
        }
        .setNeutralButton(R.string.cancel_ru, null)
        .show()
}