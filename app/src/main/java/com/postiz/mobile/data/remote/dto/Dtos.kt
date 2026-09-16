package com.postiz.mobile.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/*
 * Schema provenance, so nobody mistakes a guess for a documented fact:
 *
 * VERIFIED against the actual backend source
 * (shacky-postiz apps/backend/src/public-api/routes/v1/public.integrations.controller.ts
 * and libraries/nestjs-libraries/src/dtos/posts/*.dto.ts):
 *   - ConnectionStatusDto, IntegrationDto (GET /integrations)
 *   - GetPostsResponseDto, PostDto, PostIntegrationSummaryDto (GET /posts)
 *   - CreatePostRequestDto, PostRequestItemDto (POST /posts) --
 *     startDate/endDate are REQUIRED (@IsDateString(), no @IsOptional()) on
 *     GET /posts; shortLink/tags are REQUIRED (@IsDefined()) on POST /posts
 *     even though they're commonly empty/false.
 *   - the upload response shape (POST /upload)
 *
 * Response bodies whose shape is genuinely unknown (notifications) are left
 * as raw JsonElement in PostizApiService rather than force-fit into a class.
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

/**
 * The nested `integration` object on a post is a DIFFERENT, narrower shape
 * than the top-level IntegrationDto returned by GET /integrations -- notably
 * the platform key is `providerIdentifier`, not `identifier`, and there's no
 * `disabled`/`profile`/`customer`.
 */
@Serializable
data class PostIntegrationSummaryDto(
    val id: String,
    val providerIdentifier: String? = null,
    val name: String? = null,
    val picture: String? = null
)

@Serializable
data class PostDto(
    val id: String,
    val state: String? = null,
    val publishDate: String? = null,
    val integration: PostIntegrationSummaryDto? = null,
    val content: String? = null
)

/** GET /posts replies with an envelope, not a bare array. */
@Serializable
data class GetPostsResponseDto(
    val posts: List<PostDto> = emptyList()
)
