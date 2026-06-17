package com.linkanaizer.app.data.repository

import com.linkanaizer.app.data.api.LinkanazerApi
import com.linkanaizer.app.data.dto.*
import com.linkanaizer.app.util.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val api: LinkanazerApi,
) {
    suspend fun getCategories(): Resource<List<CategoryDto>> {
        return try {
            val response = api.getCategories()
            Resource.Success(response.categories)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch categories")
        }
    }

    suspend fun insertCategory(name: String): Resource<CategoryDto> {
        return try {
            val response = api.insertCategory(InsertCategoryRequest(name))
            Resource.Success(response.category)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to create category")
        }
    }

    suspend fun deleteCategory(name: String): Resource<String> {
        return try {
            val response = api.deleteCategory(DeleteCategoryRequest(name))
            Resource.Success(response.message)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete category")
        }
    }

    suspend fun changeVisibility(name: String, visible: Boolean): Resource<String> {
        return try {
            val response = api.changeVisibility(ChangeVisibilityRequest(name, visible))
            Resource.Success(response.message)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to change visibility")
        }
    }

    suspend fun fixEmojis(): Resource<FixEmojisResponse> {
        return try {
            val response = api.fixEmojis()
            Resource.Success(response)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fix emojis")
        }
    }
}
