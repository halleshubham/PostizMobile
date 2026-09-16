package com.postiz.mobile.data.remote

import com.postiz.mobile.data.remote.dto.ConnectionStatusDto
import com.postiz.mobile.data.remote.dto.CreatePostRequestDto
import com.postiz.mobile.data.remote.dto.GetPostsResponseDto
import com.postiz.mobile.data.remote.dto.IntegrationDto
import com.postiz.mobile.data.remote.dto.UploadResponseDto
import kotlinx.serialization.json.JsonElement
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Mirrors https://docs.postiz.com/public-api. All paths are relative to
 * PostizApiProvider's base URL, which already ends in
 * ".../api/public/v1/" (self-hosted) or ".../public/v1/" (cloud).
 */
interface PostizApiService {

    @GET("is-connected")
    suspend fun checkConnection(): ConnectionStatusDto

    @GET("integrations")
    suspend fun getIntegrations(
        @Query("group") group: String? = null
    ): List<IntegrationDto>

    @DELETE("integrations/{id}")
    suspend fun deleteIntegration(@Path("id") id: String): Response<Unit>

    /** startDate/endDate are REQUIRED server-side (@IsDateString(), not @IsOptional()). */
    @GET("posts")
    suspend fun getPosts(
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String,
        @Query("customer") customer: String? = null
    ): GetPostsResponseDto

    @POST("posts")
    suspend fun createPost(@Body request: CreatePostRequestDto): JsonElement

    @DELETE("posts/{id}")
    suspend fun deletePost(@Path("id") id: String): Response<Unit>

    @Multipart
    @POST("upload")
    suspend fun uploadFile(@Part file: MultipartBody.Part): UploadResponseDto

    /** Shape not yet confirmed -- returned raw so parsing never crashes the app. */
    @GET("notifications")
    suspend fun getNotifications(@Query("page") page: Int? = null): JsonElement
}
