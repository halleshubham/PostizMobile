package com.postiz.mobile.ui.screens.integrations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.postiz.mobile.data.remote.dto.IntegrationDto
import com.postiz.mobile.ui.components.EmptyState
import com.postiz.mobile.ui.components.FullScreenError
import com.postiz.mobile.ui.components.FullScreenLoading
import com.postiz.mobile.util.Resource

@Composable
fun IntegrationsScreen(viewModel: IntegrationsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Channels") }) }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (val s = state) {
                is Resource.Loading -> FullScreenLoading()
                is Resource.Error -> FullScreenError(s.message, onRetry = viewModel::load)
                is Resource.Success -> {
                    if (s.data.isEmpty()) {
                        EmptyState("No channels connected yet. Connect one from the Postiz web app.")
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(s.data, key = { it.id }) { integration ->
                                IntegrationCard(integration)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IntegrationCard(integration: IntegrationDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (integration.disabled)
                MaterialTheme.colorScheme.surfaceVariant
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (integration.picture != null) {
                AsyncImage(
                    model = integration.picture,
                    contentDescription = integration.name,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                )
            } else {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(integration.name.take(1).uppercase())
                    }
                }
            }

            Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                Text(integration.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = integration.identifier + (integration.profile?.let { " · @$it" } ?: ""),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                integration.customer?.let {
                    Text(
                        text = "Group: ${it.name}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (integration.disabled) {
                Text("Disabled", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
