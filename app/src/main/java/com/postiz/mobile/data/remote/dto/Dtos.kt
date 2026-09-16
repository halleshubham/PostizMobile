package com.postiz.mobile.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/*
 * Schema provenance, so nobody mistakes a guess for a documented fact:
 *
 * VERIFIED against https://docs.postiz.com/public-api (fetched directly):
 *   - ConnectionStatusDto   (GET /is-connected)
 *   - IntegrationDto        (GET /integrations)
 *   - the create-post request shape (POST /posts quick-start examples)
 *   - the upload response shape (POST /upload quick-start example)
 *
 * BEST-EFFORT / NOT independently verified (page content wasn't fetched
 * in full during this session) -- treat as a starting point and confirm
 * against https://docs.postiz.com/public-api/openapi.json before relying
 * on them, especially exact field names for PostDto and list/query params:
 *   - PostDto               (GET /posts)
 *   - NotificationDto       (GET /notifications)
 *
 * Response bodies whose shape is genuinely unknown (create-post response,
 * notifications) are left as raw JsonElement in PostizApiService rather
 * than force-fit into a data class.
 */

@Serializable
data class ConnectionStatusDto(
    val connected: Boolean
)

@Serializable
data class CustomerDto(
    val id: String,
    val name: String
)

@Serializable
data class IntegrationDto(
    val id: String,
    val name: String,
    val identifier: String,
    val picture: String? = null,
    val disabled: Boolean = false,
    val profile: String? = null,
    val customer: CustomerDto? = null
)

@Serializable
data class UploadResponseDto(
    val id: String,
    val path: String
)

@Serializable
data class PostImageDto(
    val id: String,
    val path: String
)

@Serializable
data class PostValueDto(
    val content: String,
    val image: List<PostImageDto> = emptyList()
)

@Serializable
data class PostIntegrationRefDto(
    val id: String
)

@Serializable
data class PostRequestItemDto(
    val integration: PostIntegrationRefDto,
    val value: List<PostValueDto>,
    /** Must contain at least {"__type": "<platform identifier>"}; platforms
     *  like x/linkedin/instagram accept extra keys documented per-provider
     *  at https://docs.postiz.com/public-api/providers/<platform>. */
    val settings: JsonObject
)

@Serializable
data class CreatePostRequestDto(
    /** "now" | "schedule" | "draft" */
    val type: String,
    /** ISO-8601, e.g. 2025-01-01T10:00:00.000Z */
    val date: String,
    val shortLink: Boolean = false,
    val tags: List<String> = emptyList(),
    val posts: List<PostRequestItemDto>
)

/** BEST-EFFORT shape for a scheduled/posted item returned by GET /posts. */
@Serializable
data class PostDto(
    val id: String,
    val state: String? = null,
    val publishDate: String? = null,
    val integration: IntegrationDto? = null,
    val content: String? = null
)
