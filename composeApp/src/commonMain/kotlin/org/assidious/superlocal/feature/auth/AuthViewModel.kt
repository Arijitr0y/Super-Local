package org.assidious.superlocal.feature.auth

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.assidious.superlocal.data.AuthRepository

class AuthViewModel(
    private val repo: AuthRepository = AuthRepository(),
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {
    private val _ui = MutableStateFlow(AuthUiState())
    val ui: StateFlow<AuthUiState> = _ui.asStateFlow()

    init {
        scope.launch {
            repo.isAuthenticated.collectLatest { loggedIn ->
                _ui.value = _ui.value.copy(isLoggedIn = loggedIn, isLoading = false, error = null)
            }
        }
    }

    fun signIn(email: String, password: String) = scope.launch {
        _ui.value = _ui.value.copy(isLoading = true, error = null)
        runCatching { repo.signIn(email, password) }
            .onSuccess { _ui.value = _ui.value.copy(isLoading = false) }
            .onFailure { e -> _ui.value = _ui.value.copy(isLoading = false, error = e.message) }
    }

    fun signUp(email: String, password: String) = scope.launch {
        _ui.value = _ui.value.copy(isLoading = true, error = null)
        runCatching { repo.signUp(email, password) }
            .onSuccess { _ui.value = _ui.value.copy(isLoading = false) }
            .onFailure { e -> _ui.value = _ui.value.copy(isLoading = false, error = e.message) }
    }

    fun signOut() = scope.launch {
        runCatching { repo.signOut() }
            .onFailure { e -> _ui.value = _ui.value.copy(error = e.message) }
    }
}
