package com.linkanaizer.app.ui.auth

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.linkanaizer.app.BuildConfig
import com.linkanaizer.app.R
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.linkanaizer.app.ui.components.GradientButton
import com.linkanaizer.app.ui.components.LoadingAnimation
import com.linkanaizer.app.ui.theme.*

@Composable
fun LoginScreen(
    authState: AuthUiState,
    onGoogleSignIn: (String) -> Unit,
    onEmailSignIn: (String, String) -> Unit,
    onEmailSignUp: (String, String, String) -> Unit,
    onGoogleSignInError: (String) -> Unit,
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val oneTapClient = remember { Identity.getSignInClient(context) }

    var isSignUpMode by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
                val idToken = credential.googleIdToken
                if (idToken != null) {
                    onGoogleSignIn(idToken)
                }
            } catch (e: Exception) {
                Log.e("LoginScreen", "Google sign-in failed", e)
                onGoogleSignInError("Google sign-in failed: ${e.localizedMessage}")
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp)
                .padding(top = 60.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(
                    if (isSignUpMode) R.drawable.ill_register else R.drawable.ill_login
                ),
                contentDescription = if (isSignUpMode) "Sign up illustration" else "Login illustration",
                modifier = Modifier.size(160.dp),
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Linkanaizer",
                color = GradientEnd,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = InterFont,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Save, organize, and discover\nyour links with AI",
                color = TextSecondary,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                fontFamily = InterFont,
            )

            Spacer(Modifier.height(40.dp))

            if (authState.isLoading) {
                LoadingAnimation()
            } else {
                // Name field (sign-up only)
                if (isSignUpMode) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = GradientStart,
                            unfocusedBorderColor = BorderLight,
                            focusedLabelColor = TextPrimary,
                            unfocusedLabelColor = TextTertiary,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                    )
                    Spacer(Modifier.height(12.dp))
                }

                // Email field
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = GradientStart,
                        unfocusedBorderColor = BorderLight,
                        focusedLabelColor = TextPrimary,
                        unfocusedLabelColor = TextTertiary,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next,
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                )

                Spacer(Modifier.height(12.dp))

                // Password field
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = GradientStart,
                        unfocusedBorderColor = BorderLight,
                        focusedLabelColor = TextPrimary,
                        unfocusedLabelColor = TextTertiary,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                    ),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            if (isSignUpMode) {
                                if (name.isNotBlank() && email.isNotBlank() && password.isNotBlank()) {
                                    onEmailSignUp(name, email, password)
                                }
                            } else {
                                if (email.isNotBlank() && password.isNotBlank()) {
                                    onEmailSignIn(email, password)
                                }
                            }
                        }
                    ),
                )

                Spacer(Modifier.height(20.dp))

                // Sign In / Sign Up button
                GradientButton(
                    text = if (isSignUpMode) "Create Account" else "Sign In",
                    onClick = {
                        focusManager.clearFocus()
                        if (isSignUpMode) {
                            if (name.isNotBlank() && email.isNotBlank() && password.isNotBlank()) {
                                onEmailSignUp(name, email, password)
                            }
                        } else {
                            if (email.isNotBlank() && password.isNotBlank()) {
                                onEmailSignIn(email, password)
                            }
                        }
                    },
                )

                Spacer(Modifier.height(12.dp))

                // Toggle sign-in / sign-up
                Text(
                    text = if (isSignUpMode) "Already have an account? Sign In" else "Don't have an account? Sign Up",
                    color = PrimaryLight,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable {
                        isSignUpMode = !isSignUpMode
                    },
                )

                Spacer(Modifier.height(24.dp))

                // Divider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = BorderLight,
                    )
                    Text(
                        text = "  or  ",
                        color = TextTertiary,
                        fontSize = 14.sp,
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = BorderLight,
                    )
                }

                Spacer(Modifier.height(24.dp))

                // Google Sign-In button
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = SurfaceWhite,
                    shadowElevation = 2.dp,
                    onClick = {
                        val signInRequest = BeginSignInRequest.builder()
                            .setGoogleIdTokenRequestOptions(
                                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                                    .setSupported(true)
                                    .setFilterByAuthorizedAccounts(false)
                                    .setServerClientId(BuildConfig.GOOGLE_CLIENT_ID)
                                    .build()
                            )
                            .build()

                        oneTapClient.beginSignIn(signInRequest)
                            .addOnSuccessListener { beginResult ->
                                launcher.launch(
                                    IntentSenderRequest.Builder(
                                        beginResult.pendingIntent.intentSender
                                    ).build()
                                )
                            }
                            .addOnFailureListener { e ->
                                Log.e("LoginScreen", "Google One Tap failed", e)
                                onGoogleSignInError("Google sign-in unavailable: ${e.localizedMessage}")
                            }
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_google),
                            contentDescription = "Google logo",
                            modifier = Modifier.size(24.dp),
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "Sign in with Google",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary,
                        )
                    }
                }
            }

            // Error message
            if (authState.errorMessage != null) {
                Spacer(Modifier.height(16.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0x33FF0000),
                ) {
                    Text(
                        text = authState.errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }
        }
    }
}
