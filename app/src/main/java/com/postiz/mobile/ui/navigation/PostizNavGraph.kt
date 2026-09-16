package com.postiz.mobile.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.postiz.mobile.ui.components.FullScreenLoading
import com.postiz.mobile.ui.main.MainScaffold
import com.postiz.mobile.ui.screens.connect.ConnectScreen

@Composable
fun PostizNavGraph(rootViewModel: RootViewModel = hiltViewModel()) {
    val hasSession by rootViewModel.hasSession.collectAsState()

    when (hasSession) {
        null -> FullScreenLoading()
        false -> ConnectScreen()
        true -> MainScaffold()
    }
}
