package kr.co.humancare.subsamplingviewer.presentation

import android.net.Uri

data class ViewerUiState(
    val selectedImageUri: Uri? = null,
    val rotationDegrees: Float = 0f,
)

sealed interface ViewerIntent {
    data object SelectImageClicked : ViewerIntent
    data class ImagePicked(val uri: Uri?) : ViewerIntent
    data object RotateLeftClicked : ViewerIntent
    data object RotateRightClicked : ViewerIntent
    data object ResetRotationClicked : ViewerIntent
    data class RotationGestureFinished(val angle: Float) : ViewerIntent
}

sealed interface ViewerEffect {
    data object OpenImagePicker : ViewerEffect
}
