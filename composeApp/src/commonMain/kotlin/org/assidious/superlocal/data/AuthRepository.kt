package org.assidious.superlocal.data

//import io.github.jan.supabase.auth.
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import org.assidious.superlocal.network.SupabaseClientProvider
import kotlinx.serialization.json.put
//import superlocal.core.SupabaseClientProvider
// AuthRepository.kt (or similar)

val authStatus = SupabaseClientProvider.auth.sessionStatus // Flow<SessionStatus>

suspend fun hasSession(): Boolean =
    SupabaseClientProvider.auth.currentSessionOrNull() != null


sealed interface AuthResult {
    data object Success : AuthResult
    data class NeedsEmailVerification(val message: String? = null) : AuthResult
    data class Error(val message: String) : AuthResult
}

class AuthRepository(
    private val provider: SupabaseClientProvider = SupabaseClientProvider
) {
    suspend fun isLoggedIn(): Boolean =
        SupabaseClientProvider.auth.currentSessionOrNull() != null
    private val auth get() = provider.client.auth

    suspend fun signUp(email: String, password: String, fullName: String?): AuthResult {
        return try {
            val userOrNull = auth.signUpWith(Email) {
                this.email = email
                this.password = password
                if (!fullName.isNullOrBlank()) data = kotlinx.serialization.json.buildJsonObject {
                    put("full_name", fullName)
                }
            }
            if (userOrNull == null) AuthResult.Success
            else AuthResult.NeedsEmailVerification("Check your email to verify your account.")
        } catch (t: Throwable) {
            AuthResult.Error(t.message ?: "Sign up failed")
        }
    }

    suspend fun signIn(email: String, password: String): AuthResult {
        return try {
            auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            AuthResult.Success
        } catch (t: Throwable) {
            val msg = t.message ?: "Login failed"
            val needsVerify = msg.contains("confirm", true) || msg.contains("verify", true)
            if (needsVerify) AuthResult.NeedsEmailVerification("Please verify your email, then try logging in.")
            else AuthResult.Error(msg)
        }
    }
    suspend fun sendPasswordReset(email: String, redirectUrl: String) {
        SupabaseClientProvider.auth.resetPasswordForEmail(email, redirectUrl = redirectUrl)
    }

    suspend fun updatePassword(newPassword: String) {
        SupabaseClientProvider.auth.updateUser { password = newPassword }
    }
    suspend fun signOut(): Result<Unit> = runCatching { auth.signOut() }

    suspend fun signInWithGoogle(redirectUrl: String? = null) {
        if (redirectUrl == null) {
            SupabaseClientProvider.auth.signInWith(Google) // uses platform deep link by default
        } else {
            SupabaseClientProvider.auth.signInWith(Google, redirectUrl = redirectUrl)
        }
    }
}
