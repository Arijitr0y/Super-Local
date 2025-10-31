package org.assidious.superlocal.feature.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.assidious.superlocal.data.AuthRepository
import org.assidious.superlocal.data.AuthResult

data class ForgotState(
    val loading: Boolean = false,
    val sent: Boolean = false,
    val error: String? = null
)

class AuthViewModel(
    private val repo: AuthRepository = AuthRepository(),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + Job())
) {
    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state

    fun onEmail(v: String)    { _state.value = _state.value.copy(email = v) }
    fun onPassword(v: String) { _state.value = _state.value.copy(password = v) }

    fun login() {
        val s = _state.value
        if (!s.email.contains('@')) {
            _state.value = s.copy(error = "Enter a valid email"); return
        }
        if (s.password.length < 6) {
            _state.value = s.copy(error = "Password must be at least 6 characters"); return
        }
        _state.value = s.copy(isLoading = true, error = null)
        scope.launch {
            when (val res = repo.signIn(s.email.trim(), s.password)) {
                is AuthResult.Success ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        error = null,
                        userEmail = s.email.trim()
                    )
                is AuthResult.NeedsEmailVerification ->
                    _state.value = _state.value.copy(isLoading = false, error = res.message)
                is AuthResult.Error ->
                    _state.value = _state.value.copy(isLoading = false, error = res.message)
            }
        }
    }

    fun signup(fullName: String?) {
        val s = _state.value
        if (!s.email.contains('@')) {
            _state.value = s.copy(error = "Enter a valid email"); return
        }
        if (s.password.length < 6) {
            _state.value = s.copy(error = "Password must be at least 6 characters"); return
        }
        _state.value = s.copy(isLoading = true, error = null)
        scope.launch {
            when (val res = repo.signUp(s.email.trim(), s.password, fullName?.trim())) {
                is AuthResult.Success ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        error = null,
                        userEmail = s.email.trim()
                    )
                is AuthResult.NeedsEmailVerification ->
                    _state.value = _state.value.copy(isLoading = false, error = res.message)
                is AuthResult.Error ->
                    _state.value = _state.value.copy(isLoading = false, error = res.message)
            }
        }
    }

    fun logout() {
        scope.launch {
            repo.signOut()
            _state.value = _state.value.copy(
                isLoggedIn = false,
                userEmail = null,
                error = "Signed out"
            )
        }
    }

    // ---------- Forgot Password ----------
    class ForgotPasswordViewModel(
        private val repo: AuthRepository
    ) : ViewModel() {
        var state by mutableStateOf(ForgotState())
            private set

        suspend fun submit(email: String) {
            state = state.copy(loading = true, error = null, sent = false)
            try {
                repo.sendPasswordReset(email.trim(), "superlocal://auth/callback")
                state = state.copy(sent = true)
            } catch (t: Throwable) {
                state = state.copy(error = t.message ?: "Failed to send reset email")
            } finally {
                state = state.copy(loading = false)
            }
        }
    }

    // ---------- Reset Password ----------
    class ResetPasswordViewModel(
        private val repo: AuthRepository
    ) : ViewModel() {
        var loading by mutableStateOf(false)
        var error by mutableStateOf<String?>(null)
        var success by mutableStateOf(false)

        suspend fun update(newPassword: String) {
            loading = true; error = null; success = false
            try {
                repo.updatePassword(newPassword)
                success = true
            } catch (t: Throwable) {
                error = t.message ?: "Failed to update password"
            } finally {
                loading = false
            }
        }
    }

    // ---------- Google OAuth (non-GMS) ----------
    class LoginViewModel(
        private val repo: AuthRepository
    ) : ViewModel() {
        var loading by mutableStateOf(false)
        var error by mutableStateOf<String?>(null)

        suspend fun signInWithGoogle() {
            loading = true; error = null
            try {
                repo.signInWithGoogle("superlocal://auth/callback")
            } catch (t: Throwable) {
                error = t.message ?: "Google sign-in failed"
            } finally {
                loading = false
            }
        }
    }
    fun markLoggedIn(email: String? = null) {
        val s = _state.value
        _state.value = s.copy(isLoggedIn = true, userEmail = email ?: s.userEmail, isLoading = false, error = null)
    }


}
