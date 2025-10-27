package org.assidious.superlocal.ui.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions

class ProfileTab(
    private val onSignOut: () -> Unit
) : Tab {
    override val options: TabOptions
        @Composable get() = TabOptions(index = 3u, title = "Profile")

    @Composable
    override fun Content() {
        Column(
            Modifier.fillMaxSize().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Profile", style = MaterialTheme.typography.headlineSmall)
            // TODO: Show user info here (name, email, avatar, bookings, etc.)

            Button(
                onClick = onSignOut,
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Sign out")
            }
        }
    }
}
