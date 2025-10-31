package org.assidious.superlocal

import androidx.compose.runtime.*
import cafe.adriel.voyager.navigator.tab.TabNavigator
import org.assidious.superlocal.data.AuthRepository
import org.assidious.superlocal.feature.auth.AuthScreen
import org.assidious.superlocal.ui.tabs.HomeTab // <-- your tab entry point

@Composable
fun AppRoot() {
    var authed by remember { mutableStateOf(false) }

    // If a session already exists (e.g., after Google deep-link), flip to Home
    LaunchedEffect(Unit) {
        authed = AuthRepository().isLoggedIn()
    }

    if (authed) {
        // Show your Voyager tabs
        TabNavigator(HomeTab)   // If your HomeTab is a class, use TabNavigator(HomeTab())
    } else {
        // Show auth; when it succeeds, flip to tabs
        AuthScreen(
            onAuthenticated = { authed = true }
        )
    }
}
