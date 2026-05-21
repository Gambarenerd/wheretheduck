package com.whereduck.app.ui.starnazzocall

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.whereduck.app.data.model.StarnazzoLevel
import com.whereduck.app.data.repository.AlertRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class CallPhase {
    SENDING,
    RINGING,
    RESPONDED,
    FAILED
}

data class StarnazzoCallUiState(
    val toName: String = "",
    val level: StarnazzoLevel = StarnazzoLevel.MEDIUM,
    val phase: CallPhase = CallPhase.SENDING,
    val response: String? = null, // "arrivo", "muto", "dismissed"
    val alertId: String = ""
)

@HiltViewModel
class StarnazzoCallViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val alertRepository: AlertRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StarnazzoCallUiState())
    val uiState: StateFlow<StarnazzoCallUiState> = _uiState.asStateFlow()

    init {
        val alertId = savedStateHandle.get<String>("alertId") ?: ""
        val rawToName = savedStateHandle.get<String>("toName") ?: ""
        val toName = try {
            java.net.URLDecoder.decode(rawToName, "UTF-8")
        } catch (_: Exception) { rawToName }
        val levelKey = savedStateHandle.get<String>("level") ?: "medium"

        _uiState.value = StarnazzoCallUiState(
            toName = toName,
            level = StarnazzoLevel.fromKey(levelKey),
            phase = if (alertId.isNotEmpty()) CallPhase.RINGING else CallPhase.FAILED,
            alertId = alertId
        )

        if (alertId.isNotEmpty()) {
            observeAlertStatus(alertId)
        }
    }

    fun cancelStarnazzo() {
        val alertId = _uiState.value.alertId
        if (alertId.isEmpty()) return
        viewModelScope.launch {
            try {
                alertRepository.cancelStarnazzo(alertId)
            } catch (_: Exception) { }
        }
    }

    private fun observeAlertStatus(alertId: String) {
        viewModelScope.launch {
            alertRepository.observeAlert(alertId)
                .catch {
                    _uiState.value = _uiState.value.copy(phase = CallPhase.FAILED)
                }
                .collect { data ->
                    val status = data["status"] as? String ?: ""
                    val response = data["response"] as? String

                    when {
                        response != null -> {
                            _uiState.value = _uiState.value.copy(
                                phase = CallPhase.RESPONDED,
                                response = response
                            )
                        }
                        status == "delivered" -> {
                            _uiState.value = _uiState.value.copy(
                                phase = CallPhase.RINGING
                            )
                        }
                        status == "failed" -> {
                            _uiState.value = _uiState.value.copy(
                                phase = CallPhase.FAILED
                            )
                        }
                    }
                }
        }
    }
}
