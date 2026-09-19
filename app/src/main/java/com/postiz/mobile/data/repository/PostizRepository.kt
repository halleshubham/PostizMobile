package com.postiz.mobile.data.repository

import com.postiz.mobile.data.remote.PostizApiProvider
import com.postiz.mobile.data.remote.dto.AnalyticsDataDto
import com.postiz.mobile.data.remote.dto.ApiErrorBodyDto
import com.postiz.mobile.data.remote.dto.ChangePostStatusRequestDto
import com.postiz.mobile.data.remote.dto.CreatePostRequestDto
import com.postiz.mobile.data.remote.dto.CustomerDto
import com.postiz.mobile.data.remote.dto.IntegrationDto
import com.postiz.mobile.data.remote.dto.IntegrationSettingsOutputDto
import com.postiz.mobile.data.remote.dto.PostDto
import com.postiz.mobile.data.remote.dto.UploadFromUrlRequestDto
import com.postiz.mobile.data.remote.dto.UploadResponseDto
import com.postiz.mobile.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.decodeFromJsonElement
import okhttp3.MultipartBody
import retrofit2.HttpException
import java.io.IOException
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostizRepository @Inject constructor(
    private val apiProvider: PostizApiProvider
) {
    private val errorJson = Json { ignoreUnknownKeys = true }

    suspend fun checkConnection(): Resource<Boolean> = safeCall {
        apiProvider.getService().checkConnection().connected
    }

    suspend fun getIntegrations(): Resource<List<IntegrationDto>> = safeCall {
        apiProvider.getService().getIntegrations()
    }

    suspend fun deleteIntegration(id: String): Resource<Unit> = safeCall {
        apiProvider.getService().deleteIntegration(id)
        Unit
    }

    suspend fun getIntegrationSettings(id: String): Resource<IntegrationSettingsOutputDto> = safeCall {
        apiProvider.getService().getIntegrationSettings(id).output
    }

    /** Opens as a browser tab by the caller; this just fetches the OAuth URL to open. */
    suspend fun getSocialConnectUrl(providerIdentifier: String): Resource<String> = safeCall {
        apiProvider.getService().getSocialConnectUrl(providerIdentifier).url
    }

    /** Brands/customer groups, same concept as the web app's brand filter. */
    suspend fun getGroups(): Resource<List<CustomerDto>> = safeCall {
        apiProvider.getService().getGroups()
    }

    /**
     * The server requires a date range (there's no "all posts" query). No
     * date-range picker exists in the UI yet, so default to a window wide
     * enough to cover what a self-hoster would call "the plan": 90 days
     * back (recently published) to 180 days out (scheduled ahead).
     */
    suspend fun getPosts(
        startDate: String = Instant.now().minus(90, ChronoUnit.DAYS).toString(),
        endDate: String = Instant.now().plus(180, ChronoUnit.DAYS).toString(),
        customer: String? = null
    ): Resource<List<PostDto>> = safeCall {
        apiProvider.getService().getPosts(startDate, endDate, customer).posts
    }

    suspend fun createPost(request: CreatePostRequestDto): Resource<Unit> = safeCall {
        apiProvider.getService().createPost(request)
        Unit
    }

    suspend fun deletePost(id: String): Resource<Unit> = safeCall {
        apiProvider.getService().deletePost(id)
        Unit
    }

    /** status: "draft" or "schedule" -- the only two transitions the backend allows here. */
    suspend fun changePostStatus(id: String, status: String): Resource<Unit> = safeCall {
        apiProvider.getService().changePostStatus(id, ChangePostStatusRequestDto(status))
        Unit
    }

    suspend fun findSlot(integrationId: String): Resource<String> = safeCall {
        apiProvider.getService().findSlot(integrationId).date
    }

    suspend fun uploadFile(part: MultipartBody.Part): Resource<UploadResponseDto> = safeCall {
        apiProvider.getService().uploadFile(part)
    }

    suspend fun uploadFromUrl(url: String): Resource<UploadResponseDto> = safeCall {
        apiProvider.getService().uploadFromUrl(UploadFromUrlRequestDto(url))
    }

    suspend fun getChannelAnalytics(integrationId: String, days: Int = 7): Resource<List<AnalyticsDataDto>> = safeCall {
        apiProvider.getService().getChannelAnalytics(integrationId, days.toString())
    }

    /** Returns an empty list when the backend replies {"missing": true} (post not released yet). */
    suspend fun getPostAnalytics(postId: String, days: Int = 7): Resource<List<AnalyticsDataDto>> = safeCall {
        val raw = apiProvider.getService().getPostAnalytics(postId, days.toString())
        if (raw is JsonArray) {
            errorJson.decodeFromJsonElement(ListSerializer(AnalyticsDataDto.serializer()), raw)
        } else {
            emptyList()
        }
    }

    private suspend fun <T> safeCall(block: suspend () -> T): Resource<T> = withContext(Dispatchers.IO) {
        try {
            Resource.Success(block())
        } catch (e: HttpException) {
            Resource.Error(messageFor(e))
        } catch (e: IOException) {
            Resource.Error("Can't reach the server. Check the URL and your network connection.")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Something went wrong")
        }
    }

    /**
     * The backend's error bodies aren't uniform: most handlers throw
     * HttpException({msg: "..."}, code), but post-creation validation
     * throws {statusCode, provider, name, message}. Prefer whichever
     * field is present over a generic per-status-code string.
     */
    private fun messageFor(e: HttpException): String {
        val body = e.response()?.errorBody()?.string()
        val parsed = body?.let { runCatching { errorJson.decodeFromString<ApiErrorBodyDto>(it) }.getOrNull() }
        return parsed?.message ?: parsed?.msg ?: genericMessageFor(e.code())
    }

    private fun genericMessageFor(code: Int): String = when (code) {
        401 -> "Unauthorized – check your API key"
        403 -> "Forbidden – this key can't access that resource"
        404 -> "Not found"
        413 -> "File too large"
        429 -> "Rate limit exceeded – try again in a bit"
        in 500..599 -> "Server error ($code) – try again later"
        else -> "Request failed ($code)"
    }
}
