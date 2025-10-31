package org.assidious.superlocal.feature.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.assidious.superlocal.data.AuthRepository
import org.assidious.superlocal.network.SupabaseClientProvider

@Composable
fun AuthScreen(
    vm: AuthViewModel = remember { AuthViewModel() },
    onAuthenticated: () -> Unit = {}
) {
    val state by vm.state.collectAsState(initial = AuthUiState())
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        // after Activity exchanges the code, this becomes authenticated
        val repo = org.assidious.superlocal.data.AuthRepository()
        if (repo.isLoggedIn()) vm.markLoggedIn()
    }
    if (state.isLoggedIn) {
        LaunchedEffect("authed") { onAuthenticated() }
    }
    // Local UI-only toggle and optional name for signup
    var isSignup by remember { mutableStateOf(false) }
    var fullName by remember { mutableStateOf("") }

    // Child viewmodels (use your nested classes; keep auth in one place)
    val forgotVm = remember { AuthViewModel.ForgotPasswordViewModel(AuthRepository()) }
    val googleVm = remember { AuthViewModel.LoginViewModel(AuthRepository()) }

    // Forgot dialog open state
    var showForgot by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val repo = org.assidious.superlocal.data.AuthRepository()
        if (repo.isLoggedIn()) onAuthenticated()
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SegButton(
                text = "Log in",
                selected = !isSignup,
                onClick = { isSignup = false },
                modifier = Modifier.weight(1f)
            )
            SegButton(
                text = "Sign up",
                selected = isSignup,
                onClick = { isSignup = true },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(20.dp))

        if (isSignup) {
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full name (optional)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
        }

        OutlinedTextField(
            value = state.email,
            onValueChange = vm::onEmail,
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = state.password,
            onValueChange = vm::onPassword,
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        // Row with Forgot Password (only show on login tab)
        if (!isSignup) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = { showForgot = true }, enabled = !state.isLoading) {
                    Text("Forgot password?")
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                if (isSignup) vm.signup(fullName) else vm.login()
            },
            enabled = !state.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(
                when {
                    state.isLoading && !isSignup -> "Signing in…"
                    state.isLoading && isSignup  -> "Creating account…"
                    !isSignup -> "Log in"
                    else -> "Create account"
                }
            )
        }

        // Divider “OR”
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Divider(Modifier.weight(1f))
            Text("  OR  ")
            Divider(Modifier.weight(1f))
        }

        // Google (non-GMS OAuth) button
        Spacer(Modifier.height(16.dp))
        OutlinedButton(
            onClick = {
                scope.launch {
                    googleVm.signInWithGoogle() // uses superlocal://auth/callback inside your VM
                }
            },
            enabled = !googleVm.loading,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Text(if (googleVm.loading) "Opening Google…" else "Continue with Google")
        }

        // Show general errors under the buttons
        val unifiedError = state.error ?: googleVm.error
        if (!unifiedError.isNullOrBlank()) {
            Spacer(Modifier.height(12.dp))
            Text(
                unifiedError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }

    // Forgot Password Dialog
    if (showForgot) {
        ForgotPasswordDialog(
            emailPrefill = state.email,
            loading = forgotVm.state.loading,
            sent = forgotVm.state.sent,
            error = forgotVm.state.error,
            onDismiss = { showForgot = false },
            onSend = { email ->
                scope.launch { forgotVm.submit(email) }
            }
        )
    }
}

@Composable
private fun SegButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (selected) {
        Button(onClick = onClick, modifier = modifier) { Text(text) }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) { Text(text) }
    }
}

@Composable
private fun ForgotPasswordDialog(
    emailPrefill: String,
    loading: Boolean,
    sent: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onSend: (String) -> Unit
) {
    var email by remember(emailPrefill) { mutableStateOf(emailPrefill) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reset password") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (!error.isNullOrBlank()) {
                    Text(
                        error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (sent) {
                    Text(
                        "If an account exists for this email, a reset link has been sent.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSend(email) }, enabled = !loading) {
                Text(if (loading) "Sending…" else "Send link")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
