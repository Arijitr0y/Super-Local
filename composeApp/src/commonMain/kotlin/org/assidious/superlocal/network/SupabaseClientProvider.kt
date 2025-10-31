// org/assidious/superlocal/network/SupabaseClientProvider.kt
package org.assidious.superlocal.network

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import org.assidious.superlocal.config.Env

object SupabaseClientProvider {

    private fun requireOk(url: String, key: String) {
        require(url.isNotBlank()) { "SUPABASE_URL missing" }
        require(url.startsWith("https://")) { "SUPABASE_URL must start with https://" }
        require(!url.endsWith("/")) { "SUPABASE_URL must not have trailing /" }
        require(key.isNotBlank()) { "SUPABASE_ANON_KEY missing" }
    }

    val client: SupabaseClient by lazy {
        val url = Env.SUPABASE_URL
        val key = Env.SUPABASE_ANON_KEY
        requireOk(url, key)

        createSupabaseClient(supabaseUrl = url, supabaseKey = key) {
            install(Auth)
            install(Postgrest)
            install(Storage)
        }
    }

    val auth get() = client.auth
}
