package com.linkanaizer.app.data.api

import com.linkanaizer.app.data.dto.*
import retrofit2.http.*

interface LinkanazerApi {

    @GET("ping")
    suspend fun ping(): PingResponse

    @POST("register")
    suspend fun register(@Body request: RegisterRequest): MessageResponse

    @POST("google_auth")
    suspend fun googleAuth(@Body request: GoogleAuthRequest): GoogleAuthResponse

    @POST("insert_category")
    suspend fun insertCategory(@Body request: InsertCategoryRequest): InsertCategoryResponse

    @GET("get_categories")
    suspend fun getCategories(): CategoriesResponse

    @POST("delete_category")
    suspend fun deleteCategory(@Body request: DeleteCategoryRequest): DeleteCategoryResponse

    @POST("change_visibility")
    suspend fun changeVisibility(@Body request: ChangeVisibilityRequest): MessageResponse

    @POST("process_url")
    suspend fun processUrl(@Body request: ProcessUrlRequest): ProcessUrlResponse

    @POST("get_links")
    suspend fun getLinks(@Body request: GetLinksRequest): LinksResponse

    @POST("import_text")
    suspend fun importText(@Body request: ImportTextRequest): ImportTextResponse

    @POST("import_progress")
    suspend fun importProgress(): ImportProgressResponse

    @POST("cancel_import")
    suspend fun cancelImport(): MessageResponse

    @POST("delete_link")
    suspend fun deleteLink(@Body request: DeleteLinkRequest): DeleteLinkResponse

    @POST("move_link")
    suspend fun moveLink(@Body request: MoveLinkRequest): MoveLinkResponse

    @POST("fix_emojis")
    suspend fun fixEmojis(): FixEmojisResponse

    @POST("toggle_favorite")
    suspend fun toggleFavorite(@Body request: ToggleFavoriteRequest): ToggleFavoriteResponse

    @POST("mark_read")
    suspend fun markRead(@Body request: MarkReadRequest): MarkReadResponse

    @POST("update_link")
    suspend fun updateLink(@Body request: UpdateLinkRequest): UpdateLinkResponse
}
