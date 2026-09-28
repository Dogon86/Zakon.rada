package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AppScreen
import com.example.ui.LawViewModel
import com.example.ui.screens.CatalogScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.NotesAndBookmarksScreen
import com.example.ui.screens.ReaderScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    RadaLawsApp()
                }
            }
        }
    }
}

@Composable
fun RadaLawsApp(
    viewModel: LawViewModel = viewModel()
) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "AppScreenTransition"
    ) { screen ->
        when (screen) {
            AppScreen.HOME -> HomeScreen(viewModel = viewModel)
            AppScreen.READER -> ReaderScreen(viewModel = viewModel)
            AppScreen.CATALOG -> CatalogScreen(viewModel = viewModel)
            AppScreen.NOTES_AND_BOOKMARKS -> NotesAndBookmarksScreen(viewModel = viewModel)
        }
    }
}
