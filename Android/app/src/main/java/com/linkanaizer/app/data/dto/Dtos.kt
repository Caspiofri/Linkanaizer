package com.linkanaizer.app.data.dto

import com.google.gson.annotations.SerializedName

// ── Requests ──

data class RegisterRequest(
    val name: String,
    val email: String,
    val uid: String,
)

data class GoogleAuthRequest(
    @SerializedName("id_token") val idToken: String,
)

data class ProcessUrlRequest(
    val url: String,
    @SerializedName("category_hint") val categoryHint: String? = null,
)

data class GetLinksRequest(
    @SerializedName("categoryname") val categoryName: String,
)

data class InsertCategoryRequest(
    val category: String,
)

data class DeleteCategoryRequest(
    val category: String,
)

data class ChangeVisibilityRequest(
    val category: String,
    val visible: Boolean,
)

// ── Responses ──

data class GoogleAuthResponse(
    @SerializedName("custom_token") val customToken: String,
    val uid: String,
    val email: String,
    val name: String,
)

data class ProcessUrlResponse(
    val success: Boolean,
    val name: String,
    val category: String,
)

data class CategoryDto(
    val category: String,
    val count: Int,
    val emoji: String,
    val visible: Boolean,
    val timestamp: String? = null,
)

data class LinkDto(
    val name: String,
    val url: String,
    val category: String,
    val summary: String,
    @SerializedName("category_id") val categoryId: String? = null,
    val thumbnail: String? = null,
    val tags: List<String>? = null,
    val timestamp: String? = null,
    @SerializedName("is_favorite") val isFavorite: Boolean = false,
    @SerializedName("is_read") val isRead: Boolean = false,
)

data class InsertCategoryResponse(
    val category: CategoryDto,
)

data class MessageResponse(
    val message: String,
)

data class PingResponse(
    val status: String,
)

data class CategoriesResponse(
    val uid: String,
    val categories: List<CategoryDto>,
)

data class LinksResponse(
    val links: List<LinkDto>,
)

// ── Import File ──

data class ImportTextRequest(
    val content: String,
)

data class ImportTextResponse(
    val message: String,
    @SerializedName("links_found") val linksFound: Int,
)

data class ImportProgressResponse(
    val processed: Int = 0,
    val skipped: Int = 0,
    val errors: Int = 0,
    val total: Int = 0,
    val status: String = "idle",
)

// ── Fix Emojis ──

data class FixEmojisResponse(
    val fixed: Int,
    val message: String,
)

// ── Favorite / Read / Update ──

data class ToggleFavoriteRequest(val url: String)
data class ToggleFavoriteResponse(
    val success: Boolean,
    @SerializedName("is_favorite") val isFavorite: Boolean,
)

data class MarkReadRequest(val url: String)
data class MarkReadResponse(val success: Boolean)

data class UpdateLinkRequest(
    val url: String,
    val name: String? = null,
    val summary: String? = null,
    val tags: List<String>? = null,
    @SerializedName("new_category") val newCategory: String? = null,
)
data class UpdateLinkResponse(
    val success: Boolean,
    val message: String,
)

// ── Delete / Move Link ──

data class DeleteLinkRequest(
    val url: String,
)

data class DeleteLinkResponse(
    val success: Boolean,
    val message: String,
)

data class MoveLinkRequest(
    val url: String,
    @SerializedName("new_category") val newCategory: String,
)

data class MoveLinkResponse(
    val success: Boolean,
    val message: String,
)

// ── Delete Category (enhanced) ──

data class DeleteCategoryResponse(
    val success: Boolean,
    val message: String,
    @SerializedName("deleted_links") val deletedLinks: Int? = null,
)
