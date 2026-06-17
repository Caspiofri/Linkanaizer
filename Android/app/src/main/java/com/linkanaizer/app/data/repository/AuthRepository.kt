package com.linkanaizer.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.linkanaizer.app.data.api.LinkanazerApi
import com.linkanaizer.app.data.dto.GoogleAuthRequest
import com.linkanaizer.app.data.dto.GoogleAuthResponse
import com.linkanaizer.app.data.dto.RegisterRequest
import com.linkanaizer.app.util.Resource
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: LinkanazerApi,
    private val auth: FirebaseAuth,
) {
    val currentUser: FirebaseUser? get() = auth.currentUser

    val isLoggedIn: Boolean get() = auth.currentUser != null

    suspend fun googleSignIn(idToken: String): Resource<GoogleAuthResponse> {
        return try {
            val response = api.googleAuth(GoogleAuthRequest(idToken))
            auth.signInWithCustomToken(response.customToken).await()
            Resource.Success(response)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Google sign-in failed")
        }
    }

    suspend fun emailSignIn(email: String, password: String): Resource<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: return Resource.Error("Sign-in failed")
            Resource.Success(user)
        } catch (e: Exception) {
            val message = when {
                e.message?.contains("no user record") == true -> "No account found with this email"
                e.message?.contains("password is invalid") == true -> "Wrong password"
                e.message?.contains("badly formatted") == true -> "Invalid email format"
                else -> e.message ?: "Sign-in failed"
            }
            Resource.Error(message)
        }
    }

    suspend fun emailSignUp(name: String, email: String, password: String): Resource<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: return Resource.Error("Account creation failed")
            user.updateProfile(
                UserProfileChangeRequest.Builder().setDisplayName(name).build()
            ).await()
            Resource.Success(user)
        } catch (e: Exception) {
            val message = when {
                e.message?.contains("already in use") == true -> "An account with this email already exists"
                e.message?.contains("weak password") == true -> "Password must be at least 6 characters"
                e.message?.contains("badly formatted") == true -> "Invalid email format"
                else -> e.message ?: "Sign-up failed"
            }
            Resource.Error(message)
        }
    }

    suspend fun register(name: String, email: String, uid: String): Resource<String> {
        return try {
            val response = api.register(RegisterRequest(name, email, uid))
            Resource.Success(response.message)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Registration failed")
        }
    }

    fun logout() {
        auth.signOut()
    }
}
