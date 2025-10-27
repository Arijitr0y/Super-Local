package org.assidious.superlocal

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
//import io.github.jan.supabase.auth.SessionStatus
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import org.assidious.superlocal.feature.auth.AuthScreen
import org.assidious.superlocal.feature.auth.AuthUiState
import org.assidious.superlocal.feature.auth.AuthViewModel
import org.assidious.superlocal.network.SupabaseClientProvider
import org.assidious.superlocal.ui.MainTabs

@Composable
fun AppRoot(vm: AuthViewModel = remember { AuthViewModel() }) {
    // (Your own UI state, if you need it elsewhere)
    val authUi by vm.state.collectAsState(initial = AuthUiState())

    // Collect Supabase session status directly
    val sessionStatus by SupabaseClientProvider.client
        .auth
        .sessionStatus
        .collectAsState(initial = SessionStatus.Initializing)

    when (sessionStatus) {
        SessionStatus.Initializing -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is SessionStatus.NotAuthenticated,
        is SessionStatus.RefreshFailure -> {
            AuthScreen(vm = vm) {
                // onAuthenticated: once login happens, status will flip to Authenticated
            }
        }
        is SessionStatus.Authenticated -> {
            MainTabs(onSignOut = { vm.logout() })
        }
    }
}
