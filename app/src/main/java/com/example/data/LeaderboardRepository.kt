package com.example.data

import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.CachedLeaderboardEntry
import com.example.data.local.UserStats
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

data class LeaderboardItem(
    val rank: Int,
    val username: String,
    val bestScore: Int,
    val highestTierReached: Int = 0,
    val bestScoreLevel: Int = 1,
    val difficulty: String = "Auto",
    val updatedAt: Long
)

data class LeaderboardResult(
    val entries: List<LeaderboardItem>,
    val isOffline: Boolean
)

class LeaderboardRepository(
    private val database: AppDatabase
) {
    companion object {
        private const val TAG = "LeaderboardRepo"
        private const val COLLECTION_LEADERBOARD = "leaderboard"
    }

    // TODO: add google-services.json if setting up on a new Firebase project.
    // Ensure Firestore is enabled in Firebase Console (Test or Production rules).
    private val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    fun observeUserStats(username: String): Flow<UserStats?> {
        return database.userStatsDao().getStats(username)
    }

    suspend fun getUserStats(username: String): UserStats? = withContext(Dispatchers.IO) {
        database.userStatsDao().getStatsDirect(username)
    }

    /**
     * Fix 1: Fetch and sync user stats from Room & Firestore.
     * Ensures Main Menu always reads the player's real all-time best score before display.
     */
    suspend fun fetchAndSyncUserStats(username: String): Pair<UserStats, Int> = withContext(Dispatchers.IO) {
        var localStats = database.userStatsDao().getStatsDirect(username) ?: UserStats(username = username)
        var allTimeBest = localStats.overallBestScore
        var highestTier = localStats.highestTierReached

        try {
            val doc = suspendCancellableCoroutine { continuation ->
                firestore.collection(COLLECTION_LEADERBOARD).document(username).get()
                    .addOnSuccessListener { if (continuation.isActive) continuation.resume(it) }
                    .addOnFailureListener { if (continuation.isActive) continuation.resume(null) }
            }
            if (doc != null && doc.exists()) {
                val remoteBest = (doc.getLong("bestScore") ?: 0L).toInt()
                val remoteTier = (doc.getLong("highestTierReached") ?: doc.getLong("bestScoreLevel") ?: 0L).toInt()
                if (remoteBest > allTimeBest) {
                    allTimeBest = remoteBest
                    highestTier = maxOf(highestTier, remoteTier)
                    localStats = localStats.copy(
                        overallBestScore = allTimeBest,
                        highestTierReached = highestTier,
                        updatedAt = System.currentTimeMillis()
                    )
                    database.userStatsDao().insertOrUpdate(localStats)
                } else if (allTimeBest > remoteBest) {
                    syncPersonalBestToFirestore(
                        username = username,
                        bestScore = allTimeBest,
                        tierReached = highestTier,
                        level = 1,
                        difficulty = "Auto"
                    )
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Sync user stats error: ${e.message}")
        }

        Pair(localStats, allTimeBest)
    }

    suspend fun getTop100Leaderboard(): LeaderboardResult = withContext(Dispatchers.IO) {
        try {
            val snapshot = suspendCancellableCoroutine { continuation ->
                firestore.collection(COLLECTION_LEADERBOARD)
                    .orderBy("bestScore", Query.Direction.DESCENDING)
                    .limit(100)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        if (continuation.isActive) continuation.resume(querySnapshot)
                    }
                    .addOnFailureListener { exception ->
                        if (continuation.isActive) continuation.resume(null)
                    }
            }

            if (snapshot != null && !snapshot.isEmpty) {
                var currentRank = 1
                val items = snapshot.documents.map { doc ->
                    val username = doc.getString("username") ?: doc.id
                    val score = (doc.getLong("bestScore") ?: 0L).toInt()
                    val tier = (doc.getLong("highestTierReached") ?: doc.getLong("bestScoreLevel") ?: 0L).toInt()
                    val level = (doc.getLong("bestScoreLevel") ?: 1L).toInt()
                    val diff = doc.getString("difficultyEffective") ?: doc.getString("difficulty") ?: "Auto"
                    val updated = doc.getLong("updatedAt") ?: System.currentTimeMillis()
                    LeaderboardItem(
                        rank = currentRank++,
                        username = username,
                        bestScore = score,
                        highestTierReached = tier,
                        bestScoreLevel = level,
                        difficulty = diff,
                        updatedAt = updated
                    )
                }

                // Cache in local Room DB
                val cacheEntities = items.map {
                    CachedLeaderboardEntry(
                        rank = it.rank,
                        username = it.username,
                        bestScore = it.bestScore,
                        highestTierReached = it.highestTierReached,
                        bestScoreLevel = it.bestScoreLevel,
                        difficulty = it.difficulty,
                        updatedAt = it.updatedAt
                    )
                }
                database.cachedLeaderboardDao().replaceAll(cacheEntities)

                LeaderboardResult(entries = items, isOffline = false)
            } else {
                // Fallback to room cache
                loadFromCache(isOfflineFallback = snapshot == null)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to fetch remote leaderboard: ${e.message}", e)
            loadFromCache(isOfflineFallback = true)
        }
    }

    private suspend fun loadFromCache(isOfflineFallback: Boolean): LeaderboardResult {
        val cached = database.cachedLeaderboardDao().getAllDirect()
        val items = cached.map {
            LeaderboardItem(
                rank = it.rank,
                username = it.username,
                bestScore = it.bestScore,
                highestTierReached = it.highestTierReached,
                bestScoreLevel = it.bestScoreLevel,
                difficulty = it.difficulty,
                updatedAt = it.updatedAt
            )
        }
        return LeaderboardResult(entries = items, isOffline = isOfflineFallback)
    }

    suspend fun recordGameResult(
        username: String,
        levelNumber: Int,
        difficulty: String,
        score: Int,
        stars: Int,
        violationsSliced: Int,
        trapsAvoided: Int,
        trapsSliced: Int,
        maxComboStreak: Int,
        tierReached: Int = 0,
        categoryBreakdown: Map<ComplianceCategory, Int> = emptyMap()
    ): UserStats = withContext(Dispatchers.IO) {
        val existing = database.userStatsDao().getStatsDirect(username) ?: UserStats(username = username)

        val newLevel1Best = if (levelNumber == 1) maxOf(existing.level1Best, score) else existing.level1Best
        val newLevel1Stars = if (levelNumber == 1) maxOf(existing.level1Stars, stars) else existing.level1Stars

        val newLevel2Best = if (levelNumber == 2) maxOf(existing.level2Best, score) else existing.level2Best
        val newLevel2Stars = if (levelNumber == 2) maxOf(existing.level2Stars, stars) else existing.level2Stars

        val newLevel3Best = if (levelNumber == 3) maxOf(existing.level3Best, score) else existing.level3Best
        val newLevel3Stars = if (levelNumber == 3) maxOf(existing.level3Stars, stars) else existing.level3Stars

        val newLevel4Best = if (levelNumber == 4) maxOf(existing.level4Best, score) else existing.level4Best
        val newLevel4Stars = if (levelNumber == 4) maxOf(existing.level4Stars, stars) else existing.level4Stars

        val isNewOverallBest = score > existing.overallBestScore
        val newOverallBest = if (isNewOverallBest) score else existing.overallBestScore
        val newBestLevel = if (isNewOverallBest) levelNumber else existing.bestScoreLevel
        val newBestDiff = if (isNewOverallBest) difficulty else existing.bestScoreDifficulty
        val newHighestTier = maxOf(existing.highestTierReached, tierReached)

        val briberyHits = categoryBreakdown[ComplianceCategory.BRIBERY] ?: 0
        val fraudHits = categoryBreakdown[ComplianceCategory.FRAUD] ?: 0
        val amlHits = categoryBreakdown[ComplianceCategory.MONEY_LAUNDERING] ?: 0
        val dataBreachHits = categoryBreakdown[ComplianceCategory.DATA_BREACH] ?: 0
        val systemicHits = categoryBreakdown[ComplianceCategory.SYSTEMIC_CORRUPTION] ?: 0
        val insiderHits = categoryBreakdown[ComplianceCategory.INSIDER_TRADING] ?: 0
        val conflictHits = categoryBreakdown[ComplianceCategory.CONFLICT_OF_INTEREST] ?: 0
        val embezzleHits = categoryBreakdown[ComplianceCategory.EMBEZZLEMENT] ?: 0
        val otherHits = insiderHits + conflictHits + embezzleHits

        val updatedStats = existing.copy(
            overallBestScore = newOverallBest,
            highestTierReached = newHighestTier,
            bestScoreLevel = newBestLevel,
            bestScoreDifficulty = newBestDiff,
            gamesPlayed = existing.gamesPlayed + 1,
            totalViolationsSliced = existing.totalViolationsSliced + violationsSliced,
            totalTrapsAvoided = existing.totalTrapsAvoided + trapsAvoided,
            totalTrapsSliced = existing.totalTrapsSliced + trapsSliced,
            bestComboStreak = maxOf(existing.bestComboStreak, maxComboStreak),
            briberySliced = existing.briberySliced + briberyHits,
            fraudSliced = existing.fraudSliced + fraudHits,
            moneyLaunderingSliced = existing.moneyLaunderingSliced + amlHits,
            dataBreachSliced = existing.dataBreachSliced + dataBreachHits,
            systemicCorruptionSliced = existing.systemicCorruptionSliced + systemicHits,
            otherViolationsSliced = existing.otherViolationsSliced + otherHits,
            level1Best = newLevel1Best,
            level1Stars = newLevel1Stars,
            level2Best = newLevel2Best,
            level2Stars = newLevel2Stars,
            level3Best = newLevel3Best,
            level3Stars = newLevel3Stars,
            level4Best = newLevel4Best,
            level4Stars = newLevel4Stars,
            updatedAt = System.currentTimeMillis()
        )

        database.userStatsDao().insertOrUpdate(updatedStats)

        // Record individual session
        val totalInteractions = violationsSliced + trapsSliced
        val accuracy = if (totalInteractions > 0) {
            ((violationsSliced.toFloat() / totalInteractions) * 100).toInt()
        } else 100

        try {
            database.gameSessionDao().insertSession(
                com.example.data.local.GameSessionRecord(
                    username = username,
                    levelNumber = levelNumber,
                    missionName = "Mission $levelNumber",
                    score = score,
                    violationsSliced = violationsSliced,
                    trapsAvoided = trapsAvoided,
                    trapsSliced = trapsSliced,
                    accuracyPercent = accuracy,
                    timestamp = System.currentTimeMillis()
                )
            )
        } catch (_: Exception) { }

        // Upload to Firestore if new overall personal best achieved or score > 0
        if (newOverallBest > 0) {
            syncPersonalBestToFirestore(
                username = username,
                bestScore = newOverallBest,
                tierReached = newHighestTier,
                level = newBestLevel,
                difficulty = newBestDiff
            )
        }

        updatedStats
    }

    suspend fun getRecentSessions(username: String, limit: Int = 20): List<com.example.data.local.GameSessionRecord> = withContext(Dispatchers.IO) {
        try {
            database.gameSessionDao().getRecentSessionsDirect(username)
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun getRecentSessionsFlow(username: String): Flow<List<com.example.data.local.GameSessionRecord>> {
        return database.gameSessionDao().getRecentSessions(username)
    }

    suspend fun syncPersonalBestToFirestore(
        username: String,
        bestScore: Int,
        tierReached: Int = 0,
        level: Int = 1,
        difficulty: String = "Auto"
    ) = withContext(Dispatchers.IO) {
        if (username.isBlank() || bestScore <= 0) return@withContext
        try {
            val docData = hashMapOf(
                "username" to username,
                "bestScore" to bestScore.toLong(),
                "highestTierReached" to tierReached.toLong(),
                "difficultyEffective" to "Auto",
                "bestScoreLevel" to level.toLong(),
                "difficulty" to difficulty,
                "updatedAt" to System.currentTimeMillis()
            )
            firestore.collection(COLLECTION_LEADERBOARD)
                .document(username)
                .set(docData, SetOptions.merge())
                .addOnSuccessListener {
                    Log.d(TAG, "Successfully synced personal best to Firestore for $username")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Failed to sync personal best to Firestore: ${e.message}")
                }
        } catch (e: Exception) {
            Log.w(TAG, "Firestore sync error: ${e.message}")
        }
    }
}
