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
import com.example.ui.home.CheckInScreen
import com.example.ui.home.DiscoverScreen
import com.example.ui.home.GymDetailScreen
import com.example.ui.home.GymLocation
import com.example.ui.home.HomeScreen
import com.example.ui.home.MembershipScreen
import com.example.ui.home.ProfileScreen
import com.example.ui.home.SAMPLE_GYMS
import com.example.ui.home.VisitHistoryScreen
import com.example.ui.onboarding.OnboardingScreen
import com.example.ui.owner.GymOwnerMainScreen
import com.example.ui.theme.MyApplicationTheme

enum class AppScreen {
    ONBOARDING,
    AUTH,
    GYM_OWNER_DASHBOARD,
    HOME,
    CHECK_IN,
    GYM_DETAIL,
    DISCOVER,
    MEMBERSHIP,
    PROFILE,
    VISIT_HISTORY
}

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        var currentScreen by remember { mutableStateOf(AppScreen.GYM_OWNER_DASHBOARD) }
        var selectedGymForDetail by remember { mutableStateOf<GymLocation?>(null) }

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
            AppScreen.ONBOARDING -> {
              OnboardingScreen(
                onGetStartedClick = {
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
                onBackToOnboarding = {
                  currentScreen = AppScreen.ONBOARDING
                },
                modifier = Modifier.fillMaxSize()
              )
            }
            AppScreen.HOME -> {
              HomeScreen(
                memberName = "Alex",
                onNavigateToCheckIn = {
                  currentScreen = AppScreen.CHECK_IN
                },
                onNavigateToGymDetail = { gym ->
                  selectedGymForDetail = gym
                  currentScreen = AppScreen.GYM_DETAIL
                },
                onNavigateToDiscover = {
                  currentScreen = AppScreen.DISCOVER
                },
                onNavigateToMembership = {
                  currentScreen = AppScreen.MEMBERSHIP
                },
                onNavigateToProfile = {
                  currentScreen = AppScreen.PROFILE
                },
                onNavigateToHistory = {
                  currentScreen = AppScreen.VISIT_HISTORY
                },
                onNavigateToBrowsePlans = {
                  currentScreen = AppScreen.MEMBERSHIP
                },
                modifier = Modifier.fillMaxSize()
              )
            }
            AppScreen.CHECK_IN -> {
              CheckInScreen(
                memberName = "Alex",
                gymName = selectedGymForDetail?.name ?: "Pulse Fitness",
                onBackClick = {
                  currentScreen = AppScreen.HOME
                },
                modifier = Modifier.fillMaxSize()
              )
            }
            AppScreen.GYM_DETAIL -> {
              GymDetailScreen(
                gym = selectedGymForDetail ?: SAMPLE_GYMS.first(),
                onBackClick = {
                  currentScreen = AppScreen.HOME
                },
                onCheckInClick = {
                  currentScreen = AppScreen.CHECK_IN
                },
                modifier = Modifier.fillMaxSize()
              )
            }
            AppScreen.DISCOVER -> {
              DiscoverScreen(
                onGymSelect = { gym ->
                  selectedGymForDetail = gym
                  currentScreen = AppScreen.GYM_DETAIL
                },
                onBackClick = {
                  currentScreen = AppScreen.HOME
                },
                onTabSelected = { tab ->
                  when (tab) {
                    "Home" -> currentScreen = AppScreen.HOME
                    "Membership" -> currentScreen = AppScreen.MEMBERSHIP
                    "Profile" -> currentScreen = AppScreen.PROFILE
                  }
                },
                modifier = Modifier.fillMaxSize()
              )
            }
            AppScreen.MEMBERSHIP -> {
              MembershipScreen(
                onBackClick = {
                  currentScreen = AppScreen.HOME
                },
                onTabSelected = { tab ->
                  when (tab) {
                    "Home" -> currentScreen = AppScreen.HOME
                    "Discover" -> currentScreen = AppScreen.DISCOVER
                    "Profile" -> currentScreen = AppScreen.PROFILE
                  }
                },
                modifier = Modifier.fillMaxSize()
              )
            }
            AppScreen.PROFILE -> {
              ProfileScreen(
                onBackClick = {
                  currentScreen = AppScreen.HOME
                },
                onLogOutClick = {
                  currentScreen = AppScreen.AUTH
                },
                onTabSelected = { tab ->
                  when (tab) {
                    "Home" -> currentScreen = AppScreen.HOME
                    "Discover" -> currentScreen = AppScreen.DISCOVER
                    "Membership" -> currentScreen = AppScreen.MEMBERSHIP
                  }
                },
                modifier = Modifier.fillMaxSize()
              )
            }
            AppScreen.VISIT_HISTORY -> {
              VisitHistoryScreen(
                onBackClick = {
                  currentScreen = AppScreen.HOME
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
