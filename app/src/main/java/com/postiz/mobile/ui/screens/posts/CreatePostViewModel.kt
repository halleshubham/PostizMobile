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
import javax.inject.Inject

enum class ScheduleMode { NOW, LATER }

data class CreatePostUiState(
    val content: String = "",
    val integrations: List<IntegrationDto> = emptyList(),
    val selectedIntegrationIds: Set<String> = emptySet(),
    val scheduleMode: ScheduleMode = ScheduleMode.NOW,
    /** ISO-8601 datetime, only used when scheduleMode == LATER */
    val scheduleDateIso: String = "",
    val uploadedImage: PostImageDto? = null,
    val isUploadingImage: Boolean = false,
    val isLoadingIntegrations: Boolean = true,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val submitted: Boolean = false
)

@HiltViewModel
class CreatePostViewModel @Inject constructor(
    private val repository: PostizRepository,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    var uiState by mutableStateOf(CreatePostUiState())
        private set

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

    fun onScheduleDateChange(value: String) {
        uiState = uiState.copy(scheduleDateIso = value)
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
            uiState.scheduleDateIso.ifBlank {
                uiState = uiState.copy(error = "Enter a schedule date (ISO-8601), e.g. 2025-01-01T10:00:00.000Z")
                return
            }
        }

        val images = uiState.uploadedImage?.let { listOf(it) } ?: emptyList()

        val request = CreatePostRequestDto(
            type = if (uiState.scheduleMode == ScheduleMode.NOW) "now" else "schedule",
            date = isoDate,
            posts = selected.map { integration ->
                PostRequestItemDto(
                    integration = PostIntegrationRefDto(id = integration.id),
                    value = listOf(PostValueDto(content = uiState.content, image = images)),
                    // Minimal settings: just the required __type discriminator.
                    // Platform-specific fields (per docs.postiz.com/public-api/providers/<x>)
                    // can be merged into this JsonObject as the app grows.
                    settings = buildJsonObject {
                        put("__type", JsonPrimitive(integration.identifier))
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
