package com.postiz.mobile.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.postiz.mobile.BuildConfig

@Composable
fun SettingsScreen(onBack: () -> Unit, viewModel: SettingsViewModel = hiltViewModel()) {
    val session by viewModel.session.collectAsState()
    var revealToken by remember { mutableStateOf(false) }
    val ink = MaterialTheme.colorScheme.onBackground
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    val accent = MaterialTheme.colorScheme.primary
    val hairline = MaterialTheme.colorScheme.outline

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

        Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 30.dp)) {
            Text(
                "‹ Posts",
                style = MaterialTheme.typography.bodyMedium,
                color = muted,
                modifier = Modifier.clickable(onClick = onBack)
            )
            Spacer(Modifier.height(10.dp))
            Text("SETTINGS", style = MaterialTheme.typography.labelLarge, color = accent)
            Text("Your server", style = MaterialTheme.typography.titleLarge)
        }

        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 34.dp)) {

            SettingsField(label = "CONNECTED SERVER") {
                Text(
                    session.serverUrl.ifBlank { "–" },
                    style = MaterialTheme.typography.bodyLarge,
                    color = ink
                )
                Text(
                    if (session.isCloud) "Postiz Cloud" else "Self-hosted",
                    style = MaterialTheme.typography.bodyMedium,
                    color = muted
                )
            }
            HorizontalDivider(color = hairline, thickness = 1.dp)

            SettingsField(label = "API KEY") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (revealToken) session.apiToken.ifBlank { "–" } else maskToken(session.apiToken),
                        style = MaterialTheme.typography.bodyLarge,
                        color = ink
                    )
                    Text(
                        if (revealToken) "Hide" else "Reveal",
                        style = MaterialTheme.typography.bodyMedium,
                        color = accent,
                        modifier = Modifier.clickable { revealToken = !revealToken }
                    )
                }
            }
            HorizontalDivider(color = hairline, thickness = 1.dp)

            Spacer(Modifier.height(28.dp))
            Text(
                "Disconnect this server",
                style = MaterialTheme.typography.titleMedium.copy(fontStyle = FontStyle.Italic),
                color = accent,
                modifier = Modifier.clickable(onClick = viewModel::disconnect)
            )
        }

        Spacer(Modifier.weight(1f))

        Text(
            "Postiz Mobile · v${BuildConfig.VERSION_NAME}",
            style = MaterialTheme.typography.bodyMedium,
            color = muted.copy(alpha = 0.7f),
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun SettingsField(label: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(vertical = 20.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        content()
    }
}

private fun maskToken(token: String): String {
    if (token.isBlank()) return "–"
    val visible = token.takeLast(4)
    return "••••••••$visible"
}
