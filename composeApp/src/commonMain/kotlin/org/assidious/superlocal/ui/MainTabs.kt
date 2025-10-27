package org.assidious.superlocal.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.tab.*
import org.assidious.superlocal.navigation.AppRoute
import org.assidious.superlocal.navigation.BottomBarRoutes
import org.assidious.superlocal.ui.tabs.BookingTab
import org.assidious.superlocal.ui.tabs.CategoryTab
import org.assidious.superlocal.ui.tabs.HomeTab
import org.assidious.superlocal.ui.tabs.ProfileTab

@Composable
fun MainTabs(
    onSignOut: () -> Unit
) {
    val startTab: Tab = HomeTab

    TabNavigator(startTab) { tabNavigator ->
        Scaffold(
            bottomBar = {
                NavigationBar {
                    BottomBarRoutes.forEach { route ->
                        val tab = route.toTab(onSignOut)
                        val isSelected = tabNavigator.current == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { tabNavigator.current = tab },
                            icon = {
                                Icon(
                                    imageVector = when (route) {
                                        AppRoute.HOME -> Icons.Default.Home
                                        AppRoute.CATEGORY -> Icons.Default.Category
                                        AppRoute.BOOKING -> Icons.Default.ReceiptLong
                                        AppRoute.PROFILE -> Icons.Default.Person
                                    },
                                    contentDescription = route.name
                                )
                            },
                            label = { Text(route.label()) }
                        )
                    }
                }
            }
        ) { inner ->
            Box(Modifier.fillMaxSize().padding(inner)) {
                CurrentTab()
            }
        }
    }
}

// Route helpers
private fun AppRoute.label(): String = when (this) {
    AppRoute.HOME -> "Home"
    AppRoute.CATEGORY -> "Category"
    AppRoute.BOOKING -> "Booking"
    AppRoute.PROFILE -> "Profile"
}

// Map enum to a Voyager Tab instance
private fun AppRoute.toTab(onSignOut: () -> Unit): Tab = when (this) {
    AppRoute.HOME -> HomeTab
    AppRoute.CATEGORY -> CategoryTab
    AppRoute.BOOKING -> BookingTab
    AppRoute.PROFILE -> ProfileTab(onSignOut)
}
