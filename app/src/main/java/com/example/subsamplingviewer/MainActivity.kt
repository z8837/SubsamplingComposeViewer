package com.example.subsamplingviewer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.subsamplingviewer.presentation.ViewerUiState
import com.example.subsamplingviewer.ui.SubsamplingViewerTheme
import com.example.subsamplingviewer.ui.ViewerScreen
import com.example.subsamplingviewer.ui.ViewerRoute

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SubsamplingViewerTheme {
                ViewerRoute()
            }
        }
    }
}

@Preview(name = "App Entry", showBackground = true, widthDp = 412, heightDp = 915)
@Composable
private fun MainActivityPreview() {
    SubsamplingViewerTheme {
        ViewerScreen(
            state = ViewerUiState(),
            onIntent = {},
        )
    }
}
