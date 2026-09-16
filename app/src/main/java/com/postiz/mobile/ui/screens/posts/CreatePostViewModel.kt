package com.postiz.mobile.ui.screens.posts

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.postiz.mobile.data.remote.dto.CreatePostRequestDto
import com.postiz.mobile.data.remote.dto.IntegrationDto
import com.postiz.mobile.data.remote.dto.PostImageDto
import com.postiz.mobile.data.remote.dto.PostIntegrationRefDto
import com.postiz.mobile.data.remote.dto.PostRequestItemDto
import com.postiz.mobile.data.remote.dto.PostValueDto
import com.postiz.mobile.data.repository.PostizRepository
import com.postiz.mobile.util.Resource
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

enum class ScheduleMode { NOW, LATER }

data class CreatePostUiState(
    val content: String = "",
    val integrations: List<IntegrationDto> = emptyList(),
    val selectedIntegrationIds: Set<String> = emptySet(),
    val scheduleMode: ScheduleMode = ScheduleMode.NOW,
    /**
     * Midnight UTC of the LOCAL calendar date picked in the DatePicker
     * (that's the contract of DatePickerState.selectedDateMillis — it is
     * NOT the user's local midnight). Combined with [scheduleHour]/
     * [scheduleMinute] (picked in the user's wall-clock time) at submit
     * time to build a real zoned Instant. Never sent to the API directly.
     */
    val scheduleDateMillisUtc: Long? = null,
    val scheduleHour: Int = 10,
    val scheduleMinute: Int = 0,
    val uploadedImage: PostImageDto? = null,
    val isUploadingImage: Boolean = false,
    val isLoadingIntegrations: Boolean = true,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val submitted: Boolean = false
) {
    /** The picked local date, or today if none picked yet (sensible picker default). */
    val scheduleLocalDate: LocalDate
        get() = scheduleDateMillisUtc
            ?.let { Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate() }
            ?: LocalDate.now()

    /** Human display string in the device's own timezone, e.g. "Fri, Sep 19 · 10:00 AM". */
    val scheduleDisplay: String
        get() {
            val date = scheduleLocalDate
            val dateLabel = date.format(DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault()))
            val timeLabel = LocalTime.of(scheduleHour, scheduleMinute)
                .format(DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault()))
            return "$dateLabel · $timeLabel"
        }
}

@HiltViewModel
class CreatePostViewModel @Inject constructor(
    private val repository: PostizRepository,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    var uiState by mutableStateOf(CreatePostUiState())
        private set

    /** The device's zone name, shown next to the picker so scheduling is never ambiguous. */
    val zoneLabel: String = ZoneId.systemDefault().getDisplayName(TextStyle.SHORT, Locale.getDefault())

    init {
        viewModelScope.launch {
            when (val result = repository.getIntegrations()) {
                is Resource.Success -> uiState = uiState.copy(
                    integrations = result.data,
                    isLoadingIntegrations = false
                )
                is Resource.Error -> uiState = uiState.copy(
                    isLoadingIntegrations = false,
                    error = result.message
                )
                Resource.Loading -> Unit
            }
        }
    }

    fun onContentChange(value: String) {
        uiState = uiState.copy(content = value)
    }

    fun toggleIntegration(id: String) {
        val current = uiState.selectedIntegrationIds
        uiState = uiState.copy(
            selectedIntegrationIds = if (id in current) current - id else current + id
        )
    }

    fun onScheduleModeChange(mode: ScheduleMode) {
        uiState = uiState.copy(scheduleMode = mode)
    }

    fun onScheduleDateSelected(utcMillis: Long?) {
        uiState = uiState.copy(scheduleDateMillisUtc = utcMillis)
    }

    fun onScheduleTimeSelected(hour: Int, minute: Int) {
        uiState = uiState.copy(scheduleHour = hour, scheduleMinute = minute)
    }

    fun onImagePicked(uri: Uri) {
        viewModelScope.launch {
            uiState = uiState.copy(isUploadingImage = true, error = null)
            val part = try {
                uriToMultipart(uri)
            } catch (e: Exception) {
                uiState = uiState.copy(isUploadingImage = false, error = "Couldn't read that file")
                return@launch
            }
            when (val result = repository.uploadFile(part)) {
                is Resource.Success -> uiState = uiState.copy(
                    isUploadingImage = false,
                    uploadedImage = PostImageDto(id = result.data.id, path = result.data.path)
                )
                is Resource.Error -> uiState = uiState.copy(isUploadingImage = false, error = result.message)
                Resource.Loading -> Unit
            }
        }
    }

    fun clearImage() {
        uiState = uiState.copy(uploadedImage = null)
    }

    /**
     * Combines the picked local calendar date with the picked wall-clock
     * time IN THE DEVICE'S OWN ZONE, then converts that to a true Instant.
     * This is the one place local time becomes UTC for the wire.
     */
    private fun scheduledInstant(): Instant {
        val localDateTime = LocalDateTime.of(
            uiState.scheduleLocalDate,
            LocalTime.of(uiState.scheduleHour, uiState.scheduleMinute)
        )
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant()
    }

    fun submit() {
        val selected = uiState.integrations.filter { it.id in uiState.selectedIntegrationIds }
        if (uiState.content.isBlank()) {
            uiState = uiState.copy(error = "Write something first")
            return
        }
        if (selected.isEmpty()) {
            uiState = uiState.copy(error = "Pick at least one channel")
            return
        }

        val isoDate = if (uiState.scheduleMode == ScheduleMode.NOW) {
            Instant.now().toString()
        } else {
            scheduledInstant().toString()
        }

        val images = uiState.uploadedImage?.let { listOf(it) } ?: emptyList()

        val request = CreatePostRequestDto(
            type = if (uiState.scheduleMode == ScheduleMode.NOW) "now" else "schedule",
            date = isoDate,
            posts = selected.map { integration ->
                PostRequestItemDto(
                    integration = PostIntegrationRefDto(id = integration.id),
                    value = listOf(PostValueDto(content = uiState.content, image = images)),
                    settings = buildJsonObject {
                        put("__type", JsonPrimitive(integration.identifier))
                        // These two providers reject the request (400) without
                        // their required extra field; everything else in the
                        // backend's provider list is optional beyond __type.
                        when (integration.identifier) {
                            "x" -> put("who_can_reply_post", JsonPrimitive("everyone"))
                            "instagram", "instagram-standalone" -> put("post_type", JsonPrimitive("post"))
                        }
                    }
                )
            }
        )

        viewModelScope.launch {
            uiState = uiState.copy(isSubmitting = true, error = null)
            when (val result = repository.createPost(request)) {
                is Resource.Success -> uiState = uiState.copy(isSubmitting = false, submitted = true)
                is Resource.Error -> uiState = uiState.copy(isSubmitting = false, error = result.message)
                Resource.Loading -> Unit
            }
        }
    }

    private fun uriToMultipart(uri: Uri): MultipartBody.Part {
        val resolver = appContext.contentResolver
        val mimeType = resolver.getType(uri) ?: "application/octet-stream"
        val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw IllegalStateException("Empty file")
        val body = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
        val fileName = "upload_${System.currentTimeMillis()}"
        return MultipartBody.Part.createFormData("file", fileName, body)
    }
}
