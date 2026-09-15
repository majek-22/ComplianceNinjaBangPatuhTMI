package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_stats")
data class UserStats(
    @PrimaryKey
    val username: String,
    val avatarId: Int = 1,
    val overallBestScore: Int = 0,
    val highestTierReached: Int = 0,
    val bestScoreLevel: Int = 1,
    val bestScoreDifficulty: String = "Auto",
    val gamesPlayed: Int = 0,
    val totalViolationsSliced: Int = 0,
    val totalTrapsAvoided: Int = 0,
    val totalTrapsSliced: Int = 0,
    val bestComboStreak: Int = 0,
    val briberySliced: Int = 0,
    val fraudSliced: Int = 0,
    val moneyLaunderingSliced: Int = 0,
    val dataBreachSliced: Int = 0,
    val systemicCorruptionSliced: Int = 0,
    val otherViolationsSliced: Int = 0,
    val level1Best: Int = 0,
    val level1Stars: Int = 0,
    val level2Best: Int = 0,
    val level2Stars: Int = 0,
    val level3Best: Int = 0,
    val level3Stars: Int = 0,
    val level4Best: Int = 0,
    val level4Stars: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun getBestForLevel(level: Int): Int {
        return when (level) {
            1 -> level1Best
            2 -> level2Best
            3 -> level3Best
            4 -> level4Best
            else -> 0
        }
    }

    fun getStarsForLevel(level: Int): Int {
        return when (level) {
            1 -> level1Stars
            2 -> level2Stars
            3 -> level3Stars
            4 -> level4Stars
            else -> 0
        }
    }

    fun isLevelUnlocked(level: Int): Boolean {
        return when (level) {
            1 -> true
            2 -> level1Best >= 1200
            3 -> level1Best >= 1200 && level2Best >= 2500
            4 -> level1Best >= 1200 && level2Best >= 2500 && level3Best >= 5000
            else -> false
        }
    }

    fun getUnlockScoreRequired(level: Int): Int {
        return when (level) {
            2 -> 1200
            3 -> 2500
            4 -> 5000
            else -> 0
        }
    }

    fun getRankTitle(): String {
        return when {
            overallBestScore >= 1200 -> "Chief Compliance Ninja"
            overallBestScore >= 800 -> "Senior Risk Specialist"
            overallBestScore >= 450 -> "Lead Investigator"
            overallBestScore >= 200 -> "Junior Auditor"
            else -> "Compliance Intern"
        }
    }
}
