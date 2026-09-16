package com.postiz.mobile.ui.screens.posts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.postiz.mobile.util.localDateOf
import com.postiz.mobile.util.stripHtml
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale

@Composable
fun PostsListScreen(
    onCreatePost: () -> Unit,
    viewModel: PostsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    val accent = MaterialTheme.colorScheme.primary
    val ink = MaterialTheme.colorScheme.onBackground
    val hairline = MaterialTheme.colorScheme.outline

    val weekStart = remember(selectedDate) { selectedDate.with(DayOfWeek.MONDAY) }
    val weekDays = remember(weekStart) { (0..6).map { weekStart.plusDays(it.toLong()) } }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp, start = 12.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { viewModel.shiftWeek(-7) }) {
                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Previous week", tint = muted)
            }
            Text(weekRangeLabel(weekStart), style = MaterialTheme.typography.labelLarge, color = accent)
            IconButton(onClick = { viewModel.shiftWeek(7) }) {
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Next week", tint = muted)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            weekDays.forEach { day ->
                DayPill(
                    day = day,
                    selected = day == selectedDate,
                    isToday = day == LocalDate.now(),
                    accent = accent,
                    ink = ink,
                    muted = muted,
                    onClick = { viewModel.selectDate(day) }
                )
            }
        }
        HorizontalDivider(color = hairline, thickness = 1.dp)

        Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp)) {
            Text("PLAN", style = MaterialTheme.typography.labelLarge, color = accent)
            Text(
                selectedDate.format(DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault())),
                style = MaterialTheme.typography.titleLarge
            )
            val count = (state as? Resource.Success)?.data
                ?.count { localDateOf(it.publishDate) == selectedDate } ?: 0
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
                    val dayPosts = s.data.filter { localDateOf(it.publishDate) == selectedDate }
                    if (dayPosts.isEmpty()) {
                        EmptyState("Nothing scheduled this day. Tap Write to plan one.")
                    } else {
                        LazyColumn(contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)) {
                            items(dayPosts, key = { it.id }) { post ->
                                PostRow(post, onDelete = { viewModel.delete(post.id) })
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun weekRangeLabel(weekStart: LocalDate): String {
    val weekEnd = weekStart.plusDays(6)
    val startLabel = weekStart.format(DateTimeFormatter.ofPattern("MMM d", Locale.getDefault()))
    val endLabel = if (weekStart.month == weekEnd.month) {
        weekEnd.format(DateTimeFormatter.ofPattern("d", Locale.getDefault()))
    } else {
        weekEnd.format(DateTimeFormatter.ofPattern("MMM d", Locale.getDefault()))
    }
    return "$startLabel – $endLabel".uppercase(Locale.getDefault())
}

@Composable
private fun DayPill(
    day: LocalDate,
    selected: Boolean,
    isToday: Boolean,
    accent: androidx.compose.ui.graphics.Color,
    ink: androidx.compose.ui.graphics.Color,
    muted: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick).padding(vertical = 4.dp)
    ) {
        Text(
            day.dayOfWeek.getDisplayName(JavaTextStyle.NARROW, Locale.getDefault()),
            style = MaterialTheme.typography.labelLarge,
            color = muted
        )
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (selected) accent else androidx.compose.ui.graphics.Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Text(
                day.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Normal, fontSize = 14.sp),
                color = when {
                    selected -> MaterialTheme.colorScheme.onPrimary
                    isToday -> accent
                    else -> ink
                }
            )
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
