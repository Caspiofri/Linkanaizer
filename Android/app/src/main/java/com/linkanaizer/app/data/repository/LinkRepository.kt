package com.linkanaizer.app.data.repository

import com.linkanaizer.app.data.api.LinkanazerApi
import com.linkanaizer.app.data.dto.*
import com.linkanaizer.app.util.Resource
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LinkRepository @Inject constructor(
    private val api: LinkanazerApi,
) {
    suspend fun processUrl(url: String, categoryHint: String? = null): Resource<ProcessUrlResponse> {
        return try {
            val response = api.processUrl(ProcessUrlRequest(url, categoryHint))
            Resource.Success(response)
        } catch (e: HttpException) {
            val message = when (e.code()) {
                409 -> "You've already saved this link!"
                400 -> "This doesn't look like a valid link."
                else -> "Something went wrong. Please try again."
            }
            Resource.Error(message)
        } catch (e: Exception) {
            Resource.Error("Couldn't connect to the server. Check your internet and try again.")
        }
    }

    suspend fun getLinksByCategory(categoryName: String): Resource<List<LinkDto>> {
        return try {
            val response = api.getLinks(GetLinksRequest(categoryName))
            Resource.Success(response.links)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch links")
        }
    }

    suspend fun importText(content: String): Resource<ImportTextResponse> {
        return try {
            val response = api.importText(ImportTextRequest(content))
            Resource.Success(response)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to import file")
        }
    }

    suspend fun getImportProgress(): Resource<ImportProgressResponse> {
        return try {
            val response = api.importProgress()
            Resource.Success(response)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to get progress")
        }
    }

    suspend fun cancelImport(): Resource<MessageResponse> {
        return try {
            val response = api.cancelImport()
            Resource.Success(response)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to cancel import")
        }
    }

    suspend fun deleteLink(url: String): Resource<DeleteLinkResponse> {
        return try {
            val response = api.deleteLink(DeleteLinkRequest(url))
            Resource.Success(response)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete link")
        }
    }

    suspend fun moveLink(url: String, newCategory: String): Resource<MoveLinkResponse> {
        return try {
            val response = api.moveLink(MoveLinkRequest(url, newCategory))
            Resource.Success(response)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to move link")
        }
    }

    suspend fun toggleFavorite(url: String): Resource<ToggleFavoriteResponse> {
        return try {
            val response = api.toggleFavorite(ToggleFavoriteRequest(url))
            Resource.Success(response)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to toggle favorite")
        }
    }

    suspend fun markRead(url: String): Resource<MarkReadResponse> {
        return try {
            val response = api.markRead(MarkReadRequest(url))
            Resource.Success(response)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to mark as read")
        }
    }

    suspend fun updateLink(
        url: String,
        name: String? = null,
        summary: String? = null,
        tags: List<String>? = null,
        newCategory: String? = null,
    ): Resource<UpdateLinkResponse> {
        return try {
            val response = api.updateLink(UpdateLinkRequest(url, name, summary, tags, newCategory))
            Resource.Success(response)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update link")
        }
    }
}
