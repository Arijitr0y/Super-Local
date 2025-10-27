// composeApp/src/commonMain/kotlin/org/assidious/superlocal/network/SupabaseClientProvider.kt
package org.assidious.superlocal.network

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import org.assidious.superlocal.config.Env

object SupabaseClientProvider {

    // Single shared client for KMP
    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = Env.SUPABASE_URL,
            supabaseKey = Env.SUPABASE_ANON_KEY
        ) {
            // GoTrue (Auth) plugin. No tokens here.
            install(Auth)
            // If you use Postgrest/Storage etc, add install(Postgrest) / install(Storage) here
        }
    }
}
