// composeApp/src/androidMain/kotlin/org/assidious/superlocal/env/Env.android.kt
// Env.android.kt
package org.assidious.superlocal.env

import org.assidious.superlocal.BuildConfig

actual object Env {
    actual val supabaseUrl: String = BuildConfig.SUPABASE_URL
    actual val supabaseAnonKey: String = BuildConfig.SUPABASE_ANON_KEY
}
