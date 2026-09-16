package com.postiz.mobile.data.repository

import com.postiz.mobile.data.remote.PostizApiProvider
import com.postiz.mobile.data.remote.dto.CreatePostRequestDto
import com.postiz.mobile.data.remote.dto.IntegrationDto
import com.postiz.mobile.data.remote.dto.PostDto
import com.postiz.mobile.data.remote.dto.UploadResponseDto
import com.postiz.mobile.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
    suspend fun checkConnection(): Resource<Boolean> = safeCall {
        apiProvider.getService().checkConnection().connected
    }

    suspend fun getIntegrations(): Resource<List<IntegrationDto>> = safeCall {
        apiProvider.getService().getIntegrations()
    }

    /**
     * The server requires a date range (there's no "all posts" query). No
     * date-range picker exists in the UI yet, so default to a window wide
     * enough to cover what a self-hoster would call "the plan": 90 days
     * back (recently published) to 180 days out (scheduled ahead).
     */
    suspend fun getPosts(
        startDate: String = Instant.now().minus(90, ChronoUnit.DAYS).toString(),
        endDate: String = Instant.now().plus(180, ChronoUnit.DAYS).toString()
    ): Resource<List<PostDto>> = safeCall {
        apiProvider.getService().getPosts(startDate, endDate).posts
    }

    suspend fun createPost(request: CreatePostRequestDto): Resource<Unit> = safeCall {
        apiProvider.getService().createPost(request)
        Unit
    }

    suspend fun deletePost(id: String): Resource<Unit> = safeCall {
        apiProvider.getService().deletePost(id)
        Unit
    }

    suspend fun deleteIntegration(id: String): Resource<Unit> = safeCall {
        apiProvider.getService().deleteIntegration(id)
        Unit
    }

    suspend fun uploadFile(part: MultipartBody.Part): Resource<UploadResponseDto> = safeCall {
        apiProvider.getService().uploadFile(part)
    }

    private suspend fun <T> safeCall(block: suspend () -> T): Resource<T> = withContext(Dispatchers.IO) {
        try {
            Resource.Success(block())
        } catch (e: HttpException) {
            Resource.Error(messageFor(e.code()))
        } catch (e: IOException) {
            Resource.Error("Can't reach the server. Check the URL and your network connection.")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Something went wrong")
        }
    }

    private fun messageFor(code: Int): String = when (code) {
        401 -> "Unauthorized – check your API key"
        403 -> "Forbidden – this key can't access that resource"
        404 -> "Not found"
        413 -> "File too large"
        429 -> "Rate limit exceeded – try again in a bit"
        in 500..599 -> "Server error ($code) – try again later"
        else -> "Request failed ($code)"
    }
}
