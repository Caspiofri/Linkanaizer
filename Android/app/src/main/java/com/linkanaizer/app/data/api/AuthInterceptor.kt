package com.linkanaizer.app.data.api

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Mirrors the auth pattern from apiClient.js:
 *   const user = auth.currentUser
 *   const idToken = await user.getIdToken()
 *   Authorization: Bearer {idToken}
 */
class AuthInterceptor @Inject constructor(
    private val auth: FirebaseAuth,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val builder = original.newBuilder()
            .header("Content-Type", "application/json")

        // Skip auth for endpoints that don't need it
        val path = original.url.encodedPath
        val skipAuth = path.contains("google_auth") || path.contains("ping")

        if (!skipAuth) {
            val user = auth.currentUser
            if (user != null) {
                try {
                    val tokenResult = Tasks.await(user.getIdToken(false))
                    val token = tokenResult.token
                    if (token != null) {
                        builder.header("Authorization", "Bearer $token")
                    }
                } catch (_: Exception) {
                    // If token fetch fails, send request without auth header
                }
            }
        }

        return chain.proceed(builder.build())
    }
}
