package com.postiz.mobile.ui.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.postiz.mobile.ui.navigation.Screen
import com.postiz.mobile.ui.screens.integrations.IntegrationsScreen
import com.postiz.mobile.ui.screens.posts.CreatePostScreen
import com.postiz.mobile.ui.screens.posts.PostsListScreen
import com.postiz.mobile.ui.screens.settings.SettingsScreen

private data class BottomTab(val screen: Screen, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private val bottomTabs = listOf(
    BottomTab(Screen.Posts, "Posts", Icons.Default.Article),
    BottomTab(Screen.Integrations, "Channels", Icons.Default.Share),
    BottomTab(Screen.Settings, "Settings", Icons.Default.Settings)
)

@Composable
fun MainScaffold() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            // The Create Post screen is reached via a FAB, not a tab, so we
            // hide the bottom bar there to keep focus on the composer.
            if (currentRoute != Screen.CreatePost.route) {
                NavigationBar {
                    bottomTabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.screen.route,
                            onClick = {
                                navController.navigate(tab.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Posts.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Posts.route) {
                PostsListScreen(onCreatePost = { navController.navigate(Screen.CreatePost.route) })
            }
            composable(Screen.CreatePost.route) {
                CreatePostScreen(onDone = { navController.popBackStack() })
            }
            composable(Screen.Integrations.route) {
                IntegrationsScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
        }
    }
}
