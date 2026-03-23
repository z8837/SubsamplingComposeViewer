package kr.co.humancare.subsamplingviewer.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ViewerViewModel : ViewModel() {

    private val _state = MutableStateFlow(ViewerUiState())
    val state = _state.asStateFlow()

    private val _effects = MutableSharedFlow<ViewerEffect>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val effects = _effects.asSharedFlow()

    fun onIntent(intent: ViewerIntent) {
        when (intent) {
            ViewerIntent.SelectImageClicked -> {
                viewModelScope.launch {
                    _effects.emit(ViewerEffect.OpenImagePicker)
                }
            }

            is ViewerIntent.ImagePicked -> {
                if (intent.uri == null) return
                _state.update {
                    it.copy(
                        selectedImageUri = intent.uri,
                        rotationDegrees = 0f,
                    )
                }
            }

            ViewerIntent.RotateLeftClicked -> updateRotationBy(-90f)
            ViewerIntent.RotateRightClicked -> updateRotationBy(90f)
            ViewerIntent.ResetRotationClicked -> {
                _state.update { it.copy(rotationDegrees = 0f) }
            }

            is ViewerIntent.RotationGestureFinished -> {
                _state.update { it.copy(rotationDegrees = normalizeAngle(intent.angle)) }
            }
        }
    }

    private fun updateRotationBy(delta: Float) {
        val currentUri = _state.value.selectedImageUri ?: return
        _state.update {
            it.copy(
                selectedImageUri = currentUri,
                rotationDegrees = normalizeAngle(it.rotationDegrees + delta),
            )
        }
    }

    private fun normalizeAngle(angle: Float): Float {
        return ((angle % 360f) + 360f) % 360f
    }
}
