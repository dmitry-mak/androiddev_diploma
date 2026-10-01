package ru.netology.nework.ui.events

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import ru.netology.nework.R
import ru.netology.nework.databinding.BottomInputEventParamsBinding
import ru.netology.nework.dto.EventType
import ru.netology.nework.util.DateUtils
import java.util.Calendar

class EventParamsBottomInput : BottomSheetDialogFragment() {

    private var _binding: BottomInputEventParamsBinding? = null
    private val binding get() = _binding!!

    private var millis: Long = -1L
    private var type: EventType = EventType.ONLINE

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomInputEventParamsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        millis = arguments?.getLong(ARG_MILLIS, -1L) ?: -1L

        type = runCatching {
            EventType.valueOf(arguments?.getString(ARG_TYPE) ?: EventType.ONLINE.name)
        }.getOrDefault(EventType.ONLINE)

        binding.dateInput.setText(if (millis > 0) DateUtils.formatMillisForInput(millis) else "")
        binding.typeGroup.check(
            if (type == EventType.ONLINE) R.id.radio_online else R.id.radio_offline
        )

        binding.dateInput.setOnClickListener { pickDate() }
        binding.dateInputLayout.setOnClickListener { pickDate() }

        binding.typeGroup.setOnCheckedChangeListener { _, checkedId ->
            type = if (checkedId == R.id.radio_offline) EventType.OFFLINE else EventType.ONLINE
            emitResult()
        }
    }

    private fun pickDate() {
        val base = Calendar.getInstance().apply {
            if (millis > 0) timeInMillis = millis
        }
        DatePickerDialog(
            requireContext(),
            { _, year, month, day -> pickTime(year, month, day) },
            base.get(Calendar.YEAR),
            base.get(Calendar.MONTH),
            base.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun pickTime(year: Int, month: Int, day: Int) {
        val base = Calendar.getInstance().apply {
            if (millis > 0) timeInMillis = millis
        }
        TimePickerDialog(
            requireContext(),
            { _, hour, minute ->
                millis = Calendar.getInstance().apply {
                    set(year, month, day, hour, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                binding.dateInput.setText(DateUtils.formatMillisForInput(millis))
                emitResult()
            },
            base.get(Calendar.HOUR_OF_DAY),
            base.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun emitResult() {
        if (millis <= 0) return
        parentFragmentManager.setFragmentResult(
            REQUEST_KEY,
            Bundle().apply {
                putLong(KEY_MILLIS, millis)
                putString(KEY_TYPE, type.name)
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val REQUEST_KEY = "event_params_request"
        const val KEY_MILLIS = "millis"
        const val KEY_TYPE = "type"

        private const val ARG_MILLIS = "arg_millis"
        private const val ARG_TYPE = "arg_type"

        fun newInstance(millis: Long, type: EventType) = EventParamsBottomInput().apply {
            arguments = Bundle().apply {
                putLong(ARG_MILLIS, millis)
                putString(ARG_TYPE, type.name)
            }
        }
    }
}