package org.assidious.superlocal.network

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import org.assidious.superlocal.env.Env

object SupabaseClientProvider {
    val client = createSupabaseClient(
        supabaseUrl = Env.supabaseUrl,
        supabaseKey = Env.supabaseAnonKey
    ) {
        install(Auth) // v3 auth plugin
        // Default HttpClient is fine for KMP; no extra config required.
    }
}
