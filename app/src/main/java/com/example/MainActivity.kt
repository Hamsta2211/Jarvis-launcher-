package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.ui.LauncherMainScreen
import com.example.ui.MainViewModel
import com.example.ui.theme.JarvisDarkBg
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Permission result handled
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        requestNotificationPermission()

        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsState()

            MyApplicationTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = JarvisDarkBg
                ) {
                    LauncherMainScreen(viewModel = viewModel)
                }
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // Instant response when Home button is tapped from other apps
        if (Intent.ACTION_MAIN == intent.action && intent.hasCategory(Intent.CATEGORY_HOME)) {
            viewModel.closeAllOverlays()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // If drawer, jarvis, or folder is open, close them first instead of exiting launcher
        if (viewModel.isJarvisOpen.value) {
            viewModel.closeJarvis()
            return
        }
        if (viewModel.isAppDrawerOpen.value) {
            viewModel.closeAppDrawer()
            return
        }
        if (viewModel.selectedFolder.value != null) {
            viewModel.closeFolder()
            return
        }
        if (viewModel.appPickerSlotIndex.value != null) {
            viewModel.closeAppPicker()
            return
        }
        if (viewModel.isSettingsOpen.value) {
            viewModel.closeSettings()
            return
        }
        // As a launcher, back on home screen should do nothing
    }
}
