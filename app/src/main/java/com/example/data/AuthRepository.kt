package com.example.data

import com.example.data.local.AppDatabase
import com.example.data.local.UserAccount
import com.example.data.local.UserStats
import java.security.MessageDigest

sealed class AuthResult {
    data class Success(val username: String) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class AuthRepository(
    private val database: AppDatabase,
    private val sessionManager: SessionManager
) {
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

    suspend fun isUsernameTaken(username: String): Boolean {
        val cleanUser = username.trim()
        if (cleanUser.isBlank()) return false
        return database.userAccountDao().getByUsername(cleanUser) != null
    }

    suspend fun register(username: String, password: String): AuthResult {
        val cleanUser = username.trim()
        if (cleanUser.isBlank() || password.isBlank()) {
            return AuthResult.Error("Username and password cannot be empty")
        }
        if (cleanUser.length < 3 || cleanUser.length > 10) {
            return AuthResult.Error("Username must be 3-10 characters")
        }
        if (!cleanUser.all { it.isLetterOrDigit() }) {
            return AuthResult.Error("Username can only contain letters and numbers")
        }

        val existing = database.userAccountDao().getByUsername(cleanUser)
        if (existing != null) {
            return AuthResult.Error("Username already taken")
        }

        val hash = hashPassword(password)
        val randomAvatarId = (1..10).random()
        val newAccount = UserAccount(username = cleanUser, passwordHash = hash, avatarId = randomAvatarId)
        try {
            database.userAccountDao().insert(newAccount)
            // Initialize user stats row with assigned avatar
            val initialStats = UserStats(username = cleanUser, avatarId = randomAvatarId)
            database.userStatsDao().insertOrUpdate(initialStats)
            // Save active session
            sessionManager.saveSession(cleanUser)
            return AuthResult.Success(cleanUser)
        } catch (e: Exception) {
            return AuthResult.Error(e.message ?: "Failed to create account")
        }
    }

    suspend fun login(username: String, password: String): AuthResult {
        val cleanUser = username.trim()
        if (cleanUser.isBlank() || password.isBlank()) {
            return AuthResult.Error("Username and password cannot be empty")
        }

        val account = database.userAccountDao().getByUsername(cleanUser)
            ?: return AuthResult.Error("Incorrect username or password")

        val hash = hashPassword(password)
        return if (account.passwordHash == hash) {
            // Ensure stats has an avatar assigned
            val stats = database.userStatsDao().getStatsDirect(account.username)
            val fallbackAvatarId = if (account.avatarId in 1..10) account.avatarId else ((kotlin.math.abs(account.username.hashCode()) % 10) + 1)
            if (stats == null) {
                database.userStatsDao().insertOrUpdate(UserStats(username = account.username, avatarId = fallbackAvatarId))
            } else if (stats.avatarId !in 1..10) {
                database.userStatsDao().insertOrUpdate(stats.copy(avatarId = fallbackAvatarId))
            }
            sessionManager.saveSession(account.username)
            AuthResult.Success(account.username)
        } else {
            AuthResult.Error("Incorrect username or password")
        }
    }

    suspend fun logout() {
        sessionManager.clearSession()
    }
}
