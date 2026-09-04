package com.example.service.auth

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.example.data.model.SchoolRole
import com.example.data.model.SchoolUser
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

sealed class AuthResult<out T> {
    data class Success<out T>(val data: T) : AuthResult<T>()
    data class Error(val message: String, val exception: Exception? = null) : AuthResult<Nothing>()
    object Loading : AuthResult<Nothing>()
}

class FirebaseAuthService {
    companion object {
        private const val TAG = "FirebaseAuthService"
    }

    private val auth: FirebaseAuth? by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseAuth.getInstance() not available: ${e.message}")
            null
        }
    }

    val currentUser: FirebaseUser?
        get() = auth?.currentUser

    val isUserLoggedIn: Boolean
        get() = auth?.currentUser != null

    val authStateFlow: Flow<FirebaseUser?> = callbackFlow {
        val firebaseAuth = auth
        if (firebaseAuth != null) {
            val listener = FirebaseAuth.AuthStateListener { fa ->
                trySend(fa.currentUser)
            }
            firebaseAuth.addAuthStateListener(listener)
            awaitClose { firebaseAuth.removeAuthStateListener(listener) }
        } else {
            trySend(null)
            awaitClose { }
        }
    }

    /**
     * Sign in with Email and Password
     */
    suspend fun signInWithEmail(email: String, passcode: String): AuthResult<FirebaseUser> {
        val firebaseAuth = auth ?: return AuthResult.Error("Firebase Auth is operating in local offline mode.")
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email.trim(), passcode).await()
            val user = result.user
            if (user != null) {
                AuthResult.Success(user)
            } else {
                AuthResult.Error("Authentication failed: User account not found.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "signInWithEmail error: ${e.message}", e)
            AuthResult.Error(e.localizedMessage ?: "Failed to sign in. Please verify your credentials.", e)
        }
    }

    /**
     * Create Account with Email and Password
     */
    suspend fun signUpWithEmail(email: String, passcode: String): AuthResult<FirebaseUser> {
        val firebaseAuth = auth ?: return AuthResult.Error("Firebase Auth is operating in local offline mode.")
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email.trim(), passcode).await()
            val user = result.user
            if (user != null) {
                AuthResult.Success(user)
            } else {
                AuthResult.Error("Registration failed.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "signUpWithEmail error: ${e.message}", e)
            AuthResult.Error(e.localizedMessage ?: "Failed to register account.", e)
        }
    }

    /**
     * Sign in via Google Credential Manager
     */
    suspend fun signInWithGoogle(
        context: Context,
        serverClientId: String? = null
    ): AuthResult<FirebaseUser> {
        val firebaseAuth = auth ?: return AuthResult.Error("Firebase Auth is operating in local offline mode.")
        return try {
            val credentialManager = CredentialManager.create(context)
            
            // Build GoogleIdOption
            val googleIdOptionBuilder = GetGoogleIdOption.Builder()
                .setAutoSelectEnabled(false)

            if (!serverClientId.isNullOrBlank()) {
                googleIdOptionBuilder.setServerClientId(serverClientId)
            } else {
                // Fallback default client id if not configured
                googleIdOptionBuilder.setFilterByAuthorizedAccounts(false)
            }

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOptionBuilder.build())
                .build()

            val result: GetCredentialResponse = credentialManager.getCredential(
                context = context,
                request = request
            )

            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = firebaseAuth.signInWithCredential(authCredential).await()
                val user = authResult.user
                if (user != null) {
                    AuthResult.Success(user)
                } else {
                    AuthResult.Error("Google Sign-In returned empty user profile.")
                }
            } else {
                AuthResult.Error("Unsupported credential type returned from Google Credential Manager.")
            }
        } catch (e: GetCredentialCancellationException) {
            Log.i(TAG, "Google Sign-In was cancelled by user.")
            AuthResult.Error("Sign-in cancelled.")
        } catch (e: GetCredentialException) {
            Log.e(TAG, "Google Credential Manager error: ${e.message}", e)
            AuthResult.Error(e.localizedMessage ?: "Google Sign-In service error.", e)
        } catch (e: Exception) {
            Log.e(TAG, "General Google Sign-In error: ${e.message}", e)
            AuthResult.Error(e.localizedMessage ?: "An unexpected error occurred during Google Sign-In.", e)
        }
    }

    /**
     * Send Password Reset Email
     */
    suspend fun sendPasswordReset(email: String): AuthResult<Unit> {
        val firebaseAuth = auth ?: return AuthResult.Error("Firebase Auth is operating in local offline mode.")
        return try {
            firebaseAuth.sendPasswordResetEmail(email.trim()).await()
            AuthResult.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "sendPasswordReset error: ${e.message}", e)
            AuthResult.Error(e.localizedMessage ?: "Failed to send password reset email.", e)
        }
    }

    /**
     * Sign out current user
     */
    suspend fun signOut(context: Context? = null) {
        try {
            auth?.signOut()
            if (context != null) {
                val credentialManager = CredentialManager.create(context)
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            }
            Log.d(TAG, "User signed out successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Error during signOut: ${e.message}", e)
        }
    }
}
