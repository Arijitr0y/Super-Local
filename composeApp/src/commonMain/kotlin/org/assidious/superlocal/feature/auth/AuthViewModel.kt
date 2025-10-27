package org.assidious.superlocal.feature.auth

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.assidious.superlocal.data.AuthRepository
import org.assidious.superlocal.data.AuthResult

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
            _state.value = _state.value.copy(isLoggedIn = false, userEmail = null, error = "Signed out")
        }
    }
}
