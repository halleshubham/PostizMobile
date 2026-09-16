package com.postiz.mobile.ui.navigation

sealed class Screen(val route: String) {
    data object Posts : Screen("posts")
    data object CreatePost : Screen("create_post")
    data object Integrations : Screen("integrations")
    data object Settings : Screen("settings")
}
