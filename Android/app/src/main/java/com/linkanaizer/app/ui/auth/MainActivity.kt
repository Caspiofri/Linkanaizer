package com.linkanaizer.app.ui.auth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.linkanaizer.app.ui.navigation.NavGraph
import com.linkanaizer.app.ui.theme.LinkanazerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LinkanazerTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}
