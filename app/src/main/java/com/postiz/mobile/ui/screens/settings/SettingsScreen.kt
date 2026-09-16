package com.postiz.mobile.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.postiz.mobile.BuildConfig

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val session by viewModel.session.collectAsState()
    var revealToken by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Text("Connected server", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(session.serverUrl.ifBlank { "-" }, style = MaterialTheme.typography.bodyLarge)
            Text(
                if (session.isCloud) "Postiz Cloud" else "Self-hosted",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(20.dp))
            Text("API key", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (revealToken) session.apiToken.ifBlank { "-" } else maskToken(session.apiToken),
                style = MaterialTheme.typography.bodyLarge
            )
            Button(onClick = { revealToken = !revealToken }, modifier = Modifier.padding(top = 8.dp)) {
                Text(if (revealToken) "Hide" else "Reveal")
            }

            Spacer(Modifier.height(24.dp))
            Divider()
            Spacer(Modifier.height(24.dp))

            Button(
                onClick = viewModel::disconnect,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Disconnect this server")
            }

            Spacer(Modifier.height(24.dp))
            Text(
                "Postiz Mobile • v${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun maskToken(token: String): String {
    if (token.isBlank()) return "-"
    val visible = token.takeLast(4)
    return "••••••••$visible"
}
