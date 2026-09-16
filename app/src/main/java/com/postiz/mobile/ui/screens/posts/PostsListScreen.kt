package com.postiz.mobile.ui.screens.posts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.postiz.mobile.data.remote.dto.PostDto
import com.postiz.mobile.ui.components.EmptyState
import com.postiz.mobile.ui.components.FullScreenError
import com.postiz.mobile.ui.components.FullScreenLoading
import com.postiz.mobile.util.Resource

@Composable
fun PostsListScreen(
    onCreatePost: () -> Unit,
    viewModel: PostsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Posts") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreatePost) {
                Icon(Icons.Default.Add, contentDescription = "New post")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (val s = state) {
                is Resource.Loading -> FullScreenLoading()
                is Resource.Error -> FullScreenError(s.message, onRetry = viewModel::load)
                is Resource.Success -> {
                    if (s.data.isEmpty()) {
                        EmptyState("No posts yet. Tap + to schedule one.")
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(s.data, key = { it.id }) { post ->
                                PostCard(post, onDelete = { viewModel.delete(post.id) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PostCard(post: PostDto, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = post.content?.take(140) ?: "(no preview available)",
                style = MaterialTheme.typography.bodyLarge
            )
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    post.state?.let {
                        Text(it, style = MaterialTheme.typography.labelLarge)
                    }
                    post.publishDate?.let {
                        Text(it, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}
