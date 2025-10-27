package org.assidious.superlocal.navigation

// Keep route names in one place (easy to share with analytics / deeplinks later)
enum class AppRoute {
    HOME, CATEGORY, BOOKING, PROFILE
}

// Helper to list tabs in bottom bar order
val BottomBarRoutes = listOf(
    AppRoute.HOME, AppRoute.CATEGORY, AppRoute.BOOKING, AppRoute.PROFILE
)
