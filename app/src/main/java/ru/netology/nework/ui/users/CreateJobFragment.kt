package ru.netology.nework.ui.users

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentCreateJobBinding
import ru.netology.nework.util.DateUtils


@AndroidEntryPoint
class CreateJobFragment : Fragment(R.layout.fragment_create_job) {

    private val viewModel: CreateJobViewModel by viewModels()

    private var _binding: FragmentCreateJobBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentCreateJobBinding.bind(view)

        setupTextWatchers()
        setupDates()
        setupSaveButton()
        observeState()
    }

    private fun setupTextWatchers() = with(binding) {
        jobName.doAfterTextChanged { viewModel.updateName(it?.toString().orEmpty()) }
        jobPosition.doAfterTextChanged { viewModel.updatePosition(it?.toString().orEmpty()) }
        jobLink.doAfterTextChanged { viewModel.updateLink(it?.toString().orEmpty()) }
    }

    private fun setupDates() {
        binding.chipJobDates.setOnClickListener { openDatesDialog() }

        parentFragmentManager.setFragmentResultListener(
            JobDatesDialogFragment.REQUEST_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            viewModel.setDates(
                bundle.getLong(JobDatesDialogFragment.KEY_START, -1L),
                bundle.getLong(JobDatesDialogFragment.KEY_FINISH, -1L)
            )

        }
    }

    private fun setupSaveButton() {
        binding.btnCreateJob.setOnClickListener { viewModel.saveJob() }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.state.collect { state ->
                        val start = state.startMillis
                        binding.chipJobDates.text = if (start != null) {
                            val finish = state.finishMillis
                            buildString {
                                append(DateUtils.formatChipDate(start))
                                append(" - ")
                                append(finish?.let { DateUtils.formatChipDate(it) } ?: "HB")
                            }
                        } else {
                            "Выберите дату"
                        }
                        binding.btnCreateJob.isEnabled = !state.saving
                    }
                }
                launch {
                    viewModel.error.collect { message ->
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    }
                }
                launch {
                    viewModel.jobSaved.collect {
                        findNavController().previousBackStackEntry?.savedStateHandle?.set(
                            UserProfileFragment.JOB_CHANGED_KEY, true
                        )
                        findNavController().navigateUp()
                    }
                }
            }
        }
    }

    private fun openDatesDialog() {
        val state = viewModel.state.value
        JobDatesDialogFragment
            .newInstance(state.startMillis ?: -1L, state.finishMillis ?: -1L)
            .show(parentFragmentManager, "job_dates")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}