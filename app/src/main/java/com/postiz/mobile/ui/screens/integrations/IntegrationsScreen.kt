package com.postiz.mobile.ui.screens.integrations

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.postiz.mobile.data.remote.dto.IntegrationDto
import com.postiz.mobile.ui.components.EmptyState
import com.postiz.mobile.ui.components.FullScreenError
import com.postiz.mobile.ui.components.FullScreenLoading
import com.postiz.mobile.util.Resource

@Composable
fun IntegrationsScreen(onBack: () -> Unit, viewModel: IntegrationsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    val accent = MaterialTheme.colorScheme.primary

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

        Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 30.dp)) {
            Text(
                "‹ Posts",
                style = MaterialTheme.typography.bodyMedium,
                color = muted,
                modifier = Modifier.clickable(onClick = onBack)
            )
            Spacer(Modifier.height(10.dp))
            Text("CHANNELS", style = MaterialTheme.typography.labelLarge, color = accent)
            Text("Connected", style = MaterialTheme.typography.titleLarge)
            val count = (state as? Resource.Success)?.data?.size ?: 0
            Text(
                if (count == 1) "1 channel" else "$count channels",
                style = MaterialTheme.typography.bodyMedium,
                color = muted
            )
        }

        Spacer(Modifier.height(8.dp))

        Box(modifier = Modifier.weight(1f).padding(horizontal = 24.dp)) {
            when (val s = state) {
                is Resource.Loading -> FullScreenLoading()
                is Resource.Error -> FullScreenError(s.message, onRetry = viewModel::load)
                is Resource.Success -> {
                    if (s.data.isEmpty()) {
                        EmptyState("No channels connected yet. Connect one from the Postiz web app.")
                    } else {
                        LazyColumn(contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)) {
                            items(s.data, key = { it.id }) { integration ->
                                IntegrationRow(integration)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IntegrationRow(integration: IntegrationDto) {
    val ink = MaterialTheme.colorScheme.onBackground
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    val accent = MaterialTheme.colorScheme.primary
    val hairline = MaterialTheme.colorScheme.outline

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (integration.picture != null) {
            AsyncImage(
                model = integration.picture,
                contentDescription = integration.name,
                modifier = Modifier.size(42.dp).clip(CircleShape)
            )
        } else {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(
                        integration.name.take(2).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = ink
                    )
                }
            }
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                integration.name,
                style = MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Normal),
                color = if (integration.disabled) muted else ink
            )
            val meta = integration.identifier.uppercase() +
                (integration.customer?.let { " · ${it.name.uppercase()}" } ?: "")
            Text(meta, style = MaterialTheme.typography.labelLarge, color = muted)
        }

        Text(
            text = if (integration.disabled) "Disabled" else "Active",
            style = MaterialTheme.typography.bodyMedium,
            color = if (integration.disabled) muted.copy(alpha = 0.7f) else accent
        )
    }
    HorizontalDivider(color = hairline, thickness = 1.dp)
}
