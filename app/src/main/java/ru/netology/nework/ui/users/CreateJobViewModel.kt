package ru.netology.nework.ui.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.netology.nework.dto.JobDto
import ru.netology.nework.repository.UserRepository
import ru.netology.nework.util.DateUtils
import javax.inject.Inject


data class CreateJobUiState(
    val name: String = "",
    val position: String = "",
    val link: String = "",
    val startMillis: Long? = null,
    val finishMillis: Long? = null,
    val saving: Boolean = false
)


@HiltViewModel
class CreateJobViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CreateJobUiState())
    val state = _state.asStateFlow()

    private val _error = MutableSharedFlow<String>()
    val error = _error.asSharedFlow()

    private val _jobSaved = MutableSharedFlow<Unit>()
    val jobSaved = _jobSaved.asSharedFlow()

    fun updateName(value: String) = _state.update { it.copy(name = value) }
    fun updatePosition(value: String) = _state.update { it.copy(position = value) }
    fun updateLink(value: String) = _state.update { it.copy(link = value) }

    fun setDates(start: Long, finish: Long) = _state.update {
        it.copy(
            startMillis = if (start > 0L) start else null,
            finishMillis = if (finish > 0L) finish else null
        )
    }

    fun saveJob() {
        val current = _state.value
        if (current.name.isBlank()) {
            viewModelScope.launch { _error.emit("Введите название компании") }
            return
        }
        if (current.position.isBlank()) {
            viewModelScope.launch { _error.emit("Введите вашу должность") }
            return
        }

        val start = current.startMillis
        if (start == null) {
            viewModelScope.launch { _error.emit("Укажите дату начала работы") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(saving = true) }
            val job = JobDto(
                id = 0L,
                name = current.name.trim(),
                position = current.position.trim(),
                start = DateUtils.isoFromMillis(start),
                finish = current.finishMillis?.let { DateUtils.isoFromMillis(it) },
                link = current.link.trim().takeIf { it.isNotBlank() }
            )
            userRepository.saveMyJob(job)
                .onSuccess {
                    _state.update { it.copy(saving = false) }
                    _jobSaved.emit(Unit)
                }
                .onFailure { error ->
                    _state.update { it.copy(saving = false) }
                    _error.emit(error.message ?: "Не удалось сохранить работу")
                }
        }
    }
}