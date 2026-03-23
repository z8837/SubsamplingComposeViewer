package com.example.subsamplingviewer.presentation

import android.net.Uri

data class ViewerUiState(
    val selectedImageUri: Uri? = null,
    val rotationDegrees: Float = 0f,
)

sealed interface ViewerIntent {
    data object SelectImageClicked : ViewerIntent
    data class ImagePicked(val uri: Uri?) : ViewerIntent
    data class RotationGestureFinished(val angle: Float) : ViewerIntent
}

sealed interface ViewerEffect {
    data object OpenImagePicker : ViewerEffect
}
