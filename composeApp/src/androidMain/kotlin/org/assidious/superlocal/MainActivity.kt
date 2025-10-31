package org.assidious.superlocal

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.assidious.superlocal.network.SupabaseClientProvider

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleAuthRedirect(intent)               // ← process deep link if present
        setContent { AppRoot() }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleAuthRedirect(intent)
    }

    private fun handleAuthRedirect(intent: Intent?) {
        val data = intent?.data ?: return
        if (data.scheme == "superlocal" && data.host == "auth" && data.path == "/callback") {
            lifecycleScope.launch {
                try {
                    // v3 expects String – use toString()
                    org.assidious.superlocal.network.SupabaseClientProvider
                        .auth
                        .exchangeCodeForSession(data.toString())

                    // sanity: confirm we *do* have a session now
                    val s = org.assidious.superlocal.network.SupabaseClientProvider
                        .auth
                        .currentSessionOrNull()
                    android.util.Log.d("Auth", "exchange OK, session=${s != null}")
                } catch (t: Throwable) {
                    android.util.Log.e("Auth", "exchange failed", t)
                }
            }
        }
    }

}
