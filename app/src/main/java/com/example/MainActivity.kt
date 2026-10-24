package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.ui.auth.AuthScreen
import com.example.ui.owner.GymOwnerMainScreen
import com.example.ui.theme.MyApplicationTheme

enum class AppScreen {
    AUTH,
    GYM_OWNER_DASHBOARD
}

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        var currentScreen by remember { mutableStateOf(AppScreen.GYM_OWNER_DASHBOARD) }

        Crossfade(targetState = currentScreen, label = "screen_transition") { screen ->
          when (screen) {
            AppScreen.GYM_OWNER_DASHBOARD -> {
              GymOwnerMainScreen(
                onLogout = {
                  currentScreen = AppScreen.AUTH
                },
                modifier = Modifier.fillMaxSize()
              )
            }
            AppScreen.AUTH -> {
              AuthScreen(
                onAuthComplete = {
                  currentScreen = AppScreen.GYM_OWNER_DASHBOARD
                },
                modifier = Modifier.fillMaxSize()
              )
            }
          }
        }
      }
    }
  }
}
