package com.linkanaizer.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.linkanaizer.app.ui.auth.AuthViewModel
import com.linkanaizer.app.ui.auth.LoginScreen
import com.linkanaizer.app.ui.home.HomeScreen
import com.linkanaizer.app.ui.category.CategoryScreen
import com.linkanaizer.app.ui.links.AllLinksScreen
import com.linkanaizer.app.ui.insert.InsertLinkScreen
import com.linkanaizer.app.ui.categories.CategoriesScreen
import com.linkanaizer.app.ui.importfile.ImportFileScreen
import com.linkanaizer.app.ui.settings.SettingsScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel = hiltViewModel(),
) {
    val authState by authViewModel.uiState.collectAsState()
    val startDestination = if (authState.isLoggedIn) Screen.Home.route else Screen.Login.route

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Screen.Login.route) {
            LoginScreen(
                authState = authState,
                onGoogleSignIn = authViewModel::signInWithGoogle,
                onEmailSignIn = authViewModel::signInWithEmail,
                onEmailSignUp = authViewModel::signUpWithEmail,
                onGoogleSignInError = authViewModel::setGoogleSignInError,
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                userName = authState.userName,
                onCategoryClick = { name, emoji ->
                    navController.navigate(Screen.Category.createRoute(name, emoji))
                },
                onAllLinksClick = { navController.navigate(Screen.AllLinks.route) },
                onInsertLinkClick = { navController.navigate(Screen.InsertLink.route) },
                onCategoriesClick = { navController.navigate(Screen.Categories.route) },
                onImportFileClick = { navController.navigate(Screen.ImportFile.route) },
                onSettingsClick = { navController.navigate(Screen.Settings.route) },
            )
        }

        composable(
            route = Screen.Category.route,
            arguments = listOf(
                navArgument("categoryName") { type = NavType.StringType },
                navArgument("categoryEmoji") {
                    type = NavType.StringType
                    defaultValue = ""
                },
            ),
        ) { backStackEntry ->
            val categoryName = backStackEntry.arguments?.getString("categoryName") ?: ""
            val categoryEmoji = backStackEntry.arguments?.getString("categoryEmoji") ?: ""
            CategoryScreen(
                categoryName = categoryName,
                categoryEmoji = categoryEmoji,
                onBackClick = { navController.popBackStack() },
            )
        }

        composable(Screen.AllLinks.route) {
            AllLinksScreen(
                onBackClick = { navController.popBackStack() },
            )
        }

        composable(Screen.InsertLink.route) {
            InsertLinkScreen(
                onBackClick = { navController.popBackStack() },
            )
        }

        composable(Screen.Categories.route) {
            CategoriesScreen(
                onBackClick = { navController.popBackStack() },
                onCategoryClick = { name, emoji ->
                    navController.navigate(Screen.Category.createRoute(name, emoji))
                },
            )
        }

        composable(Screen.ImportFile.route) {
            ImportFileScreen(
                onBackClick = { navController.popBackStack() },
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }
    }
}
