package com.postiz.mobile.ui.screens.posts

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.postiz.mobile.data.remote.dto.IntegrationDto

@Composable
fun CreatePostScreen(
    onDone: () -> Unit,
    viewModel: CreatePostViewModel = hiltViewModel()
) {
    val state = viewModel.uiState

    LaunchedEffect(state.submitted) {
        if (state.submitted) onDone()
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let(viewModel::onImagePicked) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("New post") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = state.content,
                onValueChange = viewModel::onContentChange,
                label = { Text("What do you want to say?") },
                minLines = 4,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))
            Text("Channels", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            if (state.isLoadingIntegrations) {
                CircularProgressIndicator(modifier = Modifier.height(24.dp))
            } else if (state.integrations.isEmpty()) {
                Text(
                    "No connected channels found. Connect one in the Postiz web app first.",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.integrations, key = { it.id }) { integration: IntegrationDto ->
                        FilterChip(
                            selected = integration.id in state.selectedIntegrationIds,
                            onClick = { viewModel.toggleIntegration(integration.id) },
                            label = { Text(integration.name) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Image (optional)", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            if (state.uploadedImage != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = state.uploadedImage.path,
                        contentDescription = null,
                        modifier = Modifier.height(72.dp)
                    )
                    IconButton(onClick = viewModel::clearImage) {
                        Icon(Icons.Default.Close, contentDescription = "Remove image")
                    }
                }
            } else {
                OutlinedButton(
                    onClick = { imagePicker.launch("image/*") },
                    enabled = !state.isUploadingImage
                ) {
                    if (state.isUploadingImage) {
                        CircularProgressIndicator(modifier = Modifier.height(18.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Image, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Attach image")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("When", style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = state.scheduleMode == ScheduleMode.NOW,
                    onClick = { viewModel.onScheduleModeChange(ScheduleMode.NOW) }
                )
                Text("Post now")
                Spacer(Modifier.width(16.dp))
                RadioButton(
                    selected = state.scheduleMode == ScheduleMode.LATER,
                    onClick = { viewModel.onScheduleModeChange(ScheduleMode.LATER) }
                )
                Text("Schedule")
            }

            if (state.scheduleMode == ScheduleMode.LATER) {
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.scheduleDateIso,
                    onValueChange = viewModel::onScheduleDateChange,
                    label = { Text("Date & time (ISO-8601)") },
                    placeholder = { Text("2025-01-01T10:00:00.000Z") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "TODO: swap for a real DatePicker/TimePicker dialog.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (state.error != null) {
                Spacer(Modifier.height(12.dp))
                Text(state.error, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = viewModel::submit,
                enabled = !state.isSubmitting,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.height(20.dp), strokeWidth = 2.dp)
                } else {
                    Text(if (state.scheduleMode == ScheduleMode.NOW) "Post now" else "Schedule")
                }
            }
        }
    }
}
