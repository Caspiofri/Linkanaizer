package com.linkanaizer.app.ui.navigation

import android.net.Uri

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Home : Screen("home")

    /**
     * Category name is a path segment (safe after Uri.encode).
     * Emoji is a query parameter — this avoids the path-separator ambiguity that
     * caused crashes with ZWJ family emoji sequences (e.g. 👨‍👩‍👧).
     */
    data object Category : Screen("category/{categoryName}?emoji={categoryEmoji}") {
        fun createRoute(categoryName: String, categoryEmoji: String): String {
            val safeName = Uri.encode(categoryName)
            val safeEmoji = Uri.encode(categoryEmoji)
            return "category/$safeName?emoji=$safeEmoji"
        }
    }

    data object AllLinks : Screen("all_links")
    data object InsertLink : Screen("insert_link")
    data object Categories : Screen("categories")
    data object ImportFile : Screen("import_file")
    data object Settings : Screen("settings")
}
