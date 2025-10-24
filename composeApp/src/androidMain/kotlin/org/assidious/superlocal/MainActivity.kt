package org.assidious.superlocal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.assidious.superlocal.feature.auth.AuthViewModel
import org.assidious.superlocal.feature.auth.LoginScreen

class MainActivity : ComponentActivity() {
    private val vm by lazy { AuthViewModel() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { LoginScreen(vm) } }
    }
}
