package kr.co.humancare.subsamplingviewer.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.roundToInt
import kr.co.humancare.R
import kr.co.humancare.subsamplingviewer.presentation.ViewerEffect
import kr.co.humancare.subsamplingviewer.presentation.ViewerIntent
import kr.co.humancare.subsamplingviewer.presentation.ViewerUiState
import kr.co.humancare.subsamplingviewer.presentation.ViewerViewModel
import kr.co.humancare.subsamplingviewer.widget.RotatableSubsamplingImageView
import androidx.core.net.toUri

private val ViewerColorScheme: ColorScheme = lightColorScheme(
    primary = Color(0xFF124C7C),
    onPrimary = Color.White,
    secondary = Color(0xFF127475),
    tertiary = Color(0xFFE27A2F),
    background = Color(0xFFF4F7FB),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE6EEF6),
    onSurface = Color(0xFF18202A),
    onSurfaceVariant = Color(0xFF506172),
)

@Composable
fun SubsamplingViewerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ViewerColorScheme,
        content = content,
    )
}

@Composable
fun ViewerRoute(viewModel: ViewerViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        viewModel.onIntent(ViewerIntent.ImagePicked(uri))
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                ViewerEffect.OpenImagePicker -> {
                    launcher.launch("image/*")
                }
            }
        }
    }

    ViewerScreen(
        state = state,
        onIntent = viewModel::onIntent,
    )
}

@Composable
fun ViewerScreen(
    state: ViewerUiState,
    onIntent: (ViewerIntent) -> Unit,
) {
    val systemBars = WindowInsets.systemBars.asPaddingValues()
    val selectedImageUri = state.selectedImageUri
    var isGalleryActionVisible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(selectedImageUri) {
        isGalleryActionVisible = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (selectedImageUri == null) {
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF040506),
                            Color(0xFF10151C),
                        ),
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF040506),
                            Color(0xFF10151C),
                        ),
                    )
                },
            ),
    ) {
        if (selectedImageUri == null) {
            EmptyViewer(
                onSelectImage = { onIntent(ViewerIntent.SelectImageClicked) },
            )
        } else {
            ViewerCanvas(
                imageUri = selectedImageUri,
                rotationDegrees = state.rotationDegrees,
                onAngleSettledChanged = { angle ->
                    onIntent(ViewerIntent.RotationGestureFinished(angle))
                },
                onViewerTapped = {
                    isGalleryActionVisible = !isGalleryActionVisible
                },
            )
        }

        if (selectedImageUri != null && isGalleryActionVisible) {
            FilledIconButton(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(
                        top = systemBars.calculateTopPadding() + 16.dp,
                        end = 16.dp,
                    ),
                onClick = {
                    isGalleryActionVisible = false
                    onIntent(ViewerIntent.SelectImageClicked)
                },
                shape = RoundedCornerShape(18.dp),
            ) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_gallery),
                    contentDescription = stringResource(R.string.select_image),
                )
            }
        }
    }
}

@Composable
private fun EmptyViewer(
    onSelectImage: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shadowElevation = 16.dp,
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = onSelectImage,
                    shape = RoundedCornerShape(18.dp),
                ) {
                    Text(
                        text = stringResource(R.string.select_image),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Text(
                    text = stringResource(R.string.viewer_empty_hint),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ViewerCanvas(
    imageUri: Uri,
    rotationDegrees: Float,
    onAngleSettledChanged: (Float) -> Unit,
    onViewerTapped: () -> Unit,
) {
    if (LocalInspectionMode.current) {
//        ViewerCanvasPreviewPlaceholder(rotationDegrees = rotationDegrees)
        return
    }

    AndroidView(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0C10)),
        factory = { context ->
            RotatableSubsamplingImageView(context).apply {
                onAngleSettled = onAngleSettledChanged
                onSingleTap = onViewerTapped
                render(imageUri, rotationDegrees)
            }
        },
        update = { view ->
            view.onAngleSettled = onAngleSettledChanged
            view.onSingleTap = onViewerTapped
            view.render(imageUri, rotationDegrees)
        },
    )
}

@Composable
private fun ViewerCanvasPreviewPlaceholder(rotationDegrees: Float) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF10151C))
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Preview Canvas",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Text(
                text = "Image rendering is replaced in Compose Preview.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.72f),
            )
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color.White.copy(alpha = 0.12f),
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    text = "Rotation ${rotationDegrees.roundToInt()} deg",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
        }
    }
}

@Preview(name = "Viewer Empty", showBackground = true, widthDp = 412, heightDp = 915)
@Composable
private fun ViewerScreenEmptyPreview() {
    SubsamplingViewerTheme {
        ViewerScreen(
            state = ViewerUiState(),
            onIntent = {},
        )
    }
}

@Preview(name = "Viewer Loaded", showBackground = true, widthDp = 412, heightDp = 915)
@Composable
private fun ViewerScreenLoadedPreview() {
    SubsamplingViewerTheme {
        ViewerScreen(
            state = ViewerUiState(
                selectedImageUri = "content://preview/sample-image".toUri(),
                rotationDegrees = 90f,
            ),
            onIntent = {},
        )
    }
}
