package com.postiz.mobile.ui.screens.posts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.postiz.mobile.data.remote.dto.PostDto
import com.postiz.mobile.ui.components.EmptyState
import com.postiz.mobile.ui.components.FullScreenError
import com.postiz.mobile.ui.components.FullScreenLoading
import com.postiz.mobile.util.Resource
import com.postiz.mobile.util.formatLocalSchedule
import com.postiz.mobile.util.stripHtml
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun PostsListScreen(
    onCreatePost: () -> Unit,
    viewModel: PostsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    val accent = MaterialTheme.colorScheme.primary

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

        Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 30.dp)) {
            Text("PLAN", style = MaterialTheme.typography.labelLarge, color = accent)
            Text(
                LocalDate.now().format(DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault())),
                style = MaterialTheme.typography.titleLarge
            )
            val count = (state as? Resource.Success)?.data?.size ?: 0
            Text(
                if (count == 1) "1 post scheduled" else "$count posts scheduled",
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
                        EmptyState("No posts yet. Tap Write to schedule one.")
                    } else {
                        LazyColumn(contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)) {
                            items(s.data, key = { it.id }) { post ->
                                PostRow(post, onDelete = { viewModel.delete(post.id) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PostRow(post: PostDto, onDelete: () -> Unit) {
    val ink = MaterialTheme.colorScheme.onBackground
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    val hairline = MaterialTheme.colorScheme.outline

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.widthIn(min = 62.dp)) {
            Text(
                text = post.integration?.providerIdentifier?.uppercase() ?: "–",
                style = MaterialTheme.typography.labelLarge,
                color = muted
            )
            Text(
                text = formatLocalSchedule(post.publishDate),
                style = MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic, fontSize = 14.sp),
                color = ink
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            post.integration?.name?.let { name ->
                Text(text = name, style = MaterialTheme.typography.bodyMedium, color = muted)
            }
            Text(
                text = stripHtml(post.content).ifBlank { "(no preview available)" }.take(140),
                style = MaterialTheme.typography.titleMedium,
                color = ink
            )
            Text(
                text = post.state ?: "Scheduled",
                style = MaterialTheme.typography.bodyMedium,
                color = muted
            )
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Close, contentDescription = "Delete", tint = muted)
        }
    }
    HorizontalDivider(color = hairline, thickness = 1.dp)
}
