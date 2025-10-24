package org.assidious.superlocal.feature.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(vm: AuthViewModel) {
    val ui by vm.ui.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(Modifier.padding(20.dp).widthIn(max = 420.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

            if (ui.isLoggedIn) {
                Text("Logged in")
                Button(onClick = { vm.signOut() }) { Text("Sign out") }
            } else {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                ui.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = { vm.signIn(email.trim(), password) }, enabled = !ui.isLoading) {
                        Text(if (ui.isLoading) "Signing in..." else "Sign in")
                    }
                    OutlinedButton(onClick = { vm.signUp(email.trim(), password) }, enabled = !ui.isLoading) {
                        Text("Sign up")
                    }
                }
            }
        }
    }
}
