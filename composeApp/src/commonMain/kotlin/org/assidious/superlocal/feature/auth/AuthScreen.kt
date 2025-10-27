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

@Composable
fun AuthScreen(
    vm: AuthViewModel = remember { AuthViewModel() },
    onAuthenticated: () -> Unit = {}
) {
    val state by vm.state.collectAsState(initial = AuthUiState())

    // Local UI-only toggle and optional name for signup
    var isSignup by remember { mutableStateOf(false) }
    var fullName by remember { mutableStateOf("") }

    if (state.isLoggedIn) {
        LaunchedEffect(Unit) { onAuthenticated() }
    }

    Column(
        Modifier.fillMaxSize().padding(20.dp),
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

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                if (isSignup) vm.signup(fullName) else vm.login()
            },
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth().height(48.dp)
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

        // Show error or a small success hint
//        when {
//            state.error != null -> {
//                Spacer(Modifier.height(10.dp))
//                Text(state.error!!, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
//            }
//            state.isLoggedIn && state.userEmail != null -> {
//                Spacer(Modifier.height(10.dp))
//                Text("Welcome ${state.userEmail}", style = MaterialTheme.typography.bodyMedium)
//                Spacer(Modifier.height(8.dp))
//                OutlinedButton(onClick = vm::logout) { Text("Sign out") }
//            }
//        }
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
