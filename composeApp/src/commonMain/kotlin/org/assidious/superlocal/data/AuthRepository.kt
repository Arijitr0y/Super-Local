// composeApp/src/commonMain/kotlin/org/assidious/superlocal/data/AuthRepository.kt
package org.assidious.superlocal.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.assidious.superlocal.network.SupabaseClientProvider

class AuthRepository(
    private val client: SupabaseClient = SupabaseClientProvider.client
) {
    val isAuthenticated: Flow<Boolean> =
        client.auth.sessionStatus.map { it is SessionStatus.Authenticated }

    suspend fun signUp(email: String, password: String) {
        client.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun signIn(email: String, password: String) {
        client.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun signOut() {
        client.auth.signOut()
    }

    fun currentUser(): UserInfo? = client.auth.currentUserOrNull()
}
