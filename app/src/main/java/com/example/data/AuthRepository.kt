package com.example.data

import android.content.Context
import android.util.Log
import com.example.ComplianceApplication
import com.example.data.local.AppDatabase
import com.example.data.local.UserAccount
import com.example.data.local.UserStats
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.security.MessageDigest
import kotlin.coroutines.resume

sealed class AuthResult {
    data class Success(val username: String) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class AuthRepository(
    private val database: AppDatabase,
    private val sessionManager: SessionManager,
    private val context: Context? = null
) {
    companion object {
        private const val TAG = "AuthRepo"
        private const val COLLECTION_USERS = "users"
    }

    private fun getFirestore(): FirebaseFirestore {
        context?.let { ctx ->
            try {
                if (FirebaseApp.getApps(ctx).isEmpty()) {
                    val defaultApp = FirebaseApp.initializeApp(ctx)
                    if (defaultApp == null) {
                        val options = FirebaseOptions.Builder()
                            .setApplicationId(ComplianceApplication.FIREBASE_APP_ID)
                            .setApiKey(ComplianceApplication.FIREBASE_API_KEY)
                            .setProjectId(ComplianceApplication.FIREBASE_PROJECT_ID)
                            .setGcmSenderId(ComplianceApplication.FIREBASE_GCM_SENDER_ID)
                            .setStorageBucket(ComplianceApplication.FIREBASE_STORAGE_BUCKET)
                            .build()
                        FirebaseApp.initializeApp(ctx.applicationContext, options)
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error ensuring Firebase init in getFirestore: ${e.message}")
            }
        }
        return FirebaseFirestore.getInstance()
    }

    /**
     * Hash password with SHA-256 for local demonstration storage.
     * Note: In production enterprise applications, use Credential Manager,
     * salted PBKDF2, bcrypt, or Firebase Auth.
     */
    fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(password.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    suspend fun isUsernameTaken(username: String): Boolean = withContext(Dispatchers.IO) {
        val cleanUser = username.trim()
        if (cleanUser.isBlank()) return@withContext false

        // 1. Check local Room DB
        val local = database.userAccountDao().getByUsername(cleanUser)
        if (local != null) return@withContext true

        // 2. Check Firebase Firestore
        try {
            val doc = withTimeoutOrNull(3000L) {
                suspendCancellableCoroutine<DocumentSnapshot?> { continuation ->
                    getFirestore().collection(COLLECTION_USERS).document(cleanUser.lowercase()).get()
                        .addOnSuccessListener { if (continuation.isActive) continuation.resume(it) }
                        .addOnFailureListener { ex ->
                            Log.w(TAG, "isUsernameTaken remote check failed: ${ex.message}")
                            if (continuation.isActive) continuation.resume(null)
                        }
                }
            }
            if (doc != null && doc.exists()) {
                return@withContext true
            }
        } catch (e: Exception) {
            Log.w(TAG, "Remote isUsernameTaken exception: ${e.message}")
        }
        false
    }

    suspend fun register(username: String, password: String): AuthResult = withContext(Dispatchers.IO) {
        val cleanUser = username.trim()
        if (cleanUser.isBlank() || password.isBlank()) {
            return@withContext AuthResult.Error("Username and password cannot be empty")
        }
        if (cleanUser.length < 3 || cleanUser.length > 10) {
            return@withContext AuthResult.Error("Username must be 3-10 characters")
        }
        if (!cleanUser.all { it.isLetterOrDigit() }) {
            return@withContext AuthResult.Error("Username can only contain letters and numbers")
        }

        // 1. Check local Room DB
        val existingLocal = database.userAccountDao().getByUsername(cleanUser)
        if (existingLocal != null) {
            return@withContext AuthResult.Error("Username already taken")
        }

        // 2. Check remote Firestore
        try {
            val remoteDoc = withTimeoutOrNull(4000L) {
                suspendCancellableCoroutine<DocumentSnapshot?> { continuation ->
                    getFirestore().collection(COLLECTION_USERS).document(cleanUser.lowercase()).get()
                        .addOnSuccessListener { if (continuation.isActive) continuation.resume(it) }
                        .addOnFailureListener { if (continuation.isActive) continuation.resume(null) }
                }
            }
            if (remoteDoc != null && remoteDoc.exists()) {
                return@withContext AuthResult.Error("Username already taken")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Remote check during register exception: ${e.message}")
        }

        val hash = hashPassword(password)
        val randomAvatarId = (1..10).random()
        val newAccount = UserAccount(username = cleanUser, passwordHash = hash, avatarId = randomAvatarId)

        // 3. Save to Firebase Firestore: store username, password, passwordHash, avatarId
        try {
            val userData = hashMapOf<String, Any>(
                "username" to cleanUser,
                "password" to password,
                "passwordHash" to hash,
                "avatarId" to randomAvatarId,
                "createdAt" to System.currentTimeMillis(),
                "updatedAt" to System.currentTimeMillis()
            )
            withTimeoutOrNull(4000L) {
                suspendCancellableCoroutine<Boolean> { continuation ->
                    getFirestore().collection(COLLECTION_USERS).document(cleanUser.lowercase())
                        .set(userData, SetOptions.merge())
                        .addOnSuccessListener { if (continuation.isActive) continuation.resume(true) }
                        .addOnFailureListener { ex ->
                            Log.e(TAG, "Failed to save user to Firestore: ${ex.message}", ex)
                            if (continuation.isActive) continuation.resume(false)
                        }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Firestore register write exception: ${e.message}", e)
        }

        // 4. Save to local Room DB
        try {
            database.userAccountDao().insert(newAccount)
            val initialStats = UserStats(username = cleanUser, avatarId = randomAvatarId)
            database.userStatsDao().insertOrUpdate(initialStats)
            sessionManager.saveSession(cleanUser)
            AuthResult.Success(cleanUser)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Failed to create account")
        }
    }

    suspend fun login(username: String, password: String): AuthResult = withContext(Dispatchers.IO) {
        val cleanUser = username.trim()
        if (cleanUser.isBlank() || password.isBlank()) {
            return@withContext AuthResult.Error("Username and password cannot be empty")
        }

        val hash = hashPassword(password)
        val localAccount = database.userAccountDao().getByUsername(cleanUser)

        // 1. If local account exists and matches, fast-path login with background Firestore backup sync
        if (localAccount != null && localAccount.passwordHash == hash) {
            val stats = database.userStatsDao().getStatsDirect(localAccount.username)
            val fallbackAvatarId = if (localAccount.avatarId in 1..10) localAccount.avatarId else ((kotlin.math.abs(localAccount.username.hashCode()) % 10) + 1)
            if (stats == null) {
                database.userStatsDao().insertOrUpdate(UserStats(username = localAccount.username, avatarId = fallbackAvatarId))
            } else if (stats.avatarId !in 1..10) {
                database.userStatsDao().insertOrUpdate(stats.copy(avatarId = fallbackAvatarId))
            }
            sessionManager.saveSession(localAccount.username)

            // Ensure credentials exist in Firebase Firestore (as requested by user)
            try {
                val syncData = hashMapOf<String, Any>(
                    "username" to localAccount.username,
                    "password" to password,
                    "passwordHash" to hash,
                    "avatarId" to fallbackAvatarId,
                    "updatedAt" to System.currentTimeMillis()
                )
                getFirestore().collection(COLLECTION_USERS).document(localAccount.username.lowercase())
                    .set(syncData, SetOptions.merge())
            } catch (e: Exception) {
                Log.w(TAG, "Background sync to Firestore on login failed: ${e.message}")
            }

            return@withContext AuthResult.Success(localAccount.username)
        }

        // 2. Local account not found (e.g. user uninstalled & reinstalled the app!) OR local password mismatch:
        // Query Firebase Firestore!
        var remoteDoc: DocumentSnapshot? = null
        try {
            remoteDoc = withTimeoutOrNull(5000L) {
                suspendCancellableCoroutine { continuation ->
                    getFirestore().collection(COLLECTION_USERS).document(cleanUser.lowercase()).get()
                        .addOnSuccessListener { if (continuation.isActive) continuation.resume(it) }
                        .addOnFailureListener { ex ->
                            Log.w(TAG, "Firestore user lookup on login failed: ${ex.message}")
                            if (continuation.isActive) continuation.resume(null)
                        }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firestore login query exception: ${e.message}")
        }

        if (remoteDoc != null && remoteDoc.exists()) {
            val remoteUser = remoteDoc.getString("username") ?: cleanUser
            val remotePassword = remoteDoc.getString("password")
            val remoteHash = remoteDoc.getString("passwordHash")
            val remoteAvatar = (remoteDoc.getLong("avatarId") ?: 1L).toInt()

            val isPasswordValid = (remotePassword != null && remotePassword == password) ||
                    (remoteHash != null && remoteHash == hash)

            if (isPasswordValid) {
                val effectiveAvatar = if (remoteAvatar in 1..10) remoteAvatar else 1
                val accountToSave = UserAccount(
                    username = remoteUser,
                    passwordHash = remoteHash ?: hash,
                    avatarId = effectiveAvatar
                )

                // Restore user account in local Room DB
                try {
                    if (localAccount == null) {
                        database.userAccountDao().insert(accountToSave)
                    } else {
                        database.userAccountDao().updatePassword(remoteUser, remoteHash ?: hash)
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed inserting restored account to Room: ${e.message}")
                }

                // Restore / Ensure UserStats in local Room DB
                val stats = database.userStatsDao().getStatsDirect(remoteUser)
                if (stats == null) {
                    database.userStatsDao().insertOrUpdate(UserStats(username = remoteUser, avatarId = effectiveAvatar))
                }

                // If remote document didn't have password field yet, write it now
                if (remotePassword == null) {
                    try {
                        getFirestore().collection(COLLECTION_USERS).document(remoteUser.lowercase())
                            .set(mapOf("password" to password, "passwordHash" to hash, "updatedAt" to System.currentTimeMillis()), SetOptions.merge())
                    } catch (e: Exception) { /* ignore */ }
                }

                sessionManager.saveSession(remoteUser)
                return@withContext AuthResult.Success(remoteUser)
            } else {
                return@withContext AuthResult.Error("Incorrect username or password")
            }
        }

        // 3. Fallback: if remote was unreachable (remoteDoc == null) and localAccount is null
        if (remoteDoc == null && localAccount == null) {
            return@withContext AuthResult.Error("Server Offline: Tidak dapat terhubung ke Firebase untuk memeriksa akun.")
        }

        AuthResult.Error("Incorrect username or password")
    }

    suspend fun resetPassword(username: String, newPass: String): AuthResult = withContext(Dispatchers.IO) {
        val cleanUser = username.trim()
        if (cleanUser.isBlank() || newPass.isBlank()) {
            return@withContext AuthResult.Error("Username and new password cannot be empty")
        }
        if (newPass.length < 4) {
            return@withContext AuthResult.Error("Password must be at least 4 characters")
        }

        val newHash = hashPassword(newPass)
        var userFound = false
        var targetUsername = cleanUser

        // Check local Room DB
        val localAccount = database.userAccountDao().getByUsername(cleanUser)
        if (localAccount != null) {
            database.userAccountDao().updatePassword(localAccount.username, newHash)
            userFound = true
            targetUsername = localAccount.username
        }

        // Check & update in Firebase Firestore
        try {
            val doc = withTimeoutOrNull(4000L) {
                suspendCancellableCoroutine<DocumentSnapshot?> { continuation ->
                    getFirestore().collection(COLLECTION_USERS).document(cleanUser.lowercase()).get()
                        .addOnSuccessListener { if (continuation.isActive) continuation.resume(it) }
                        .addOnFailureListener { if (continuation.isActive) continuation.resume(null) }
                }
            }
            if (doc != null && doc.exists()) {
                val remoteUser = doc.getString("username") ?: cleanUser
                val remoteAvatar = (doc.getLong("avatarId") ?: 1L).toInt()
                targetUsername = remoteUser
                userFound = true

                // Update in Firestore
                val updateData = hashMapOf<String, Any>(
                    "password" to newPass,
                    "passwordHash" to newHash,
                    "updatedAt" to System.currentTimeMillis()
                )
                getFirestore().collection(COLLECTION_USERS).document(cleanUser.lowercase())
                    .set(updateData, SetOptions.merge())

                // Also restore/update in local Room DB if not present
                if (localAccount == null) {
                    database.userAccountDao().insert(
                        UserAccount(
                            username = remoteUser,
                            passwordHash = newHash,
                            avatarId = remoteAvatar
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "resetPassword Firestore update failed: ${e.message}")
        }

        if (!userFound) {
            return@withContext AuthResult.Error("User '$cleanUser' not found")
        }

        AuthResult.Success(targetUsername)
    }

    suspend fun logout() {
        sessionManager.clearSession()
    }
}
