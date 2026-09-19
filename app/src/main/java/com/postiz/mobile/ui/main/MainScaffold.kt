package com.postiz.mobile.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.postiz.mobile.ui.navigation.Screen
import com.postiz.mobile.ui.screens.analytics.ChannelAnalyticsScreen
import com.postiz.mobile.ui.screens.analytics.PostAnalyticsScreen
import com.postiz.mobile.ui.screens.integrations.AddChannelScreen
import com.postiz.mobile.ui.screens.integrations.IntegrationsScreen
import com.postiz.mobile.ui.screens.posts.CreatePostScreen
import com.postiz.mobile.ui.screens.posts.PostsListScreen
import com.postiz.mobile.ui.screens.settings.SettingsScreen

private fun NavController.navigateSingleTopTo(route: String) =
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }

/** Full-screen detail routes reached via a drill-in, not a footer tab -- no footer on these. */
private fun String?.isDetailRoute(): Boolean =
    this == Screen.CreatePost.route ||
        this == Screen.AddChannel.route ||
        this?.startsWith("channel_analytics/") == true ||
        this?.startsWith("post_analytics/") == true

@Composable
fun MainScaffold() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (!currentRoute.isDetailRoute()) {
                EditorialFooterNav(
                    currentRoute = currentRoute,
                    onChannels = { navController.navigateSingleTopTo(Screen.Integrations.route) },
                    onWrite = { navController.navigate(Screen.CreatePost.route) },
                    onSettings = { navController.navigateSingleTopTo(Screen.Settings.route) }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Posts.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Posts.route) {
                PostsListScreen(
                    onCreatePost = { navController.navigate(Screen.CreatePost.route) },
                    onPostTap = { postId -> navController.navigate(Screen.PostAnalytics.route(postId)) }
                )
            }
            composable(Screen.CreatePost.route) {
                CreatePostScreen(onDone = { navController.popBackStack() })
            }
            composable(Screen.Integrations.route) {
                IntegrationsScreen(
                    onBack = { navController.navigateSingleTopTo(Screen.Posts.route) },
                    onAddChannel = { navController.navigate(Screen.AddChannel.route) },
                    onChannelTap = { id -> navController.navigate(Screen.ChannelAnalytics.route(id)) }
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(onBack = { navController.navigateSingleTopTo(Screen.Posts.route) })
            }
            composable(Screen.AddChannel.route) {
                AddChannelScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.ChannelAnalytics.route) {
                ChannelAnalyticsScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.PostAnalytics.route) {
                PostAnalyticsScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}

@Composable
private fun EditorialFooterNav(
    currentRoute: String?,
    onChannels: () -> Unit,
    onWrite: () -> Unit,
    onSettings: () -> Unit
) {
    val hairline = MaterialTheme.colorScheme.outline
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .drawBehind {
                drawLine(hairline, androidx.compose.ui.geometry.Offset(0f, 0f), androidx.compose.ui.geometry.Offset(size.width, 0f), strokeWidth = 1.dp.toPx())
            }
            // enableEdgeToEdge() draws content behind the system nav bar; without
            // this the footer (and its taps) end up underneath the nav buttons.
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        FooterLink("CHANNELS", active = currentRoute == Screen.Integrations.route, onClick = onChannels)
        WritePill(onClick = onWrite)
        FooterLink("SETTINGS", active = currentRoute == Screen.Settings.route, onClick = onSettings)
    }
}

@Composable
private fun FooterLink(label: String, active: Boolean, onClick: () -> Unit) {
    val color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        color = color,
        modifier = Modifier
            .clickable(onClick = onClick)
            .then(
                if (active) {
                    Modifier.drawBehind {
                        val y = size.height + 4.dp.toPx()
                        drawLine(
                            color,
                            androidx.compose.ui.geometry.Offset(0f, y),
                            androidx.compose.ui.geometry.Offset(size.width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                } else Modifier
            )
            .padding(bottom = 4.dp)
    )
}

@Composable
private fun WritePill(onClick: () -> Unit) {
    Text(
        text = "Write",
        style = MaterialTheme.typography.titleMedium.copy(fontStyle = FontStyle.Italic),
        color = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 28.dp, vertical = 12.dp)
    )
}
