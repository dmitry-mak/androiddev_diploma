package ru.netology.nework.ui.users

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import ru.netology.nework.databinding.DialogJobDatesBinding
import ru.netology.nework.util.DateUtils
import java.util.Calendar


class JobDatesDialogFragment : DialogFragment() {

    private var _binding: DialogJobDatesBinding? = null
    private val binding get() = _binding!!

    private var startMillis: Long = -1L
    private var finishMillis: Long = -1L

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogJobDatesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startMillis = arguments?.getLong(ARG_START, -1L) ?: -1L
        finishMillis = arguments?.getLong(ARG_FINISH, -1L) ?: -1L
        renderDates()

        binding.dateStartInput.setOnClickListener {
            selectDate(isStart = true)
        }

        binding.dateFinishInput.setOnClickListener {
            selectDate(isStart = false)
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnOk.setOnClickListener {
            val result = Bundle().apply {
                putLong(KEY_START, startMillis)
                putLong(KEY_FINISH,finishMillis)
            }
            parentFragmentManager.setFragmentResult(REQUEST_KEY,result)
            dismiss()
        }

    }

    private fun renderDates() {
        binding.dateStartInput.setText(
            if (startMillis > 0L) DateUtils.formatChipDate(startMillis) else ""
        )
        binding.dateFinishInput.setText(
            if (finishMillis > 0L) DateUtils.formatChipDate(finishMillis) else ""
        )
    }

    private fun selectDate(isStart: Boolean) {
        val current = if (isStart) startMillis else finishMillis
        val base = Calendar.getInstance().apply {
            if (current > 0L) timeInMillis = current
        }
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                val millis = Calendar.getInstance().apply {
                    set(year, month, day, 12, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                if (isStart) {
                    startMillis = millis
                    if (finishMillis in 1L until millis) finishMillis = -1L
                } else {
                    finishMillis = millis
                }
                renderDates()
            },
            base.get(Calendar.YEAR),
            base.get(Calendar.MONTH),
            base.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val REQUEST_KEY = "job_dates_request"
        const val KEY_START = "start"
        const val KEY_FINISH = "finish"
        private const val ARG_START = "arg_start"
        private const val ARG_FINISH = "arg_finish"

        fun newInstance(start: Long, finish: Long) = JobDatesDialogFragment().apply {
            arguments = Bundle().apply {
                putLong(ARG_START, start)
                putLong(ARG_FINISH, finish)
            }
        }
    }
}