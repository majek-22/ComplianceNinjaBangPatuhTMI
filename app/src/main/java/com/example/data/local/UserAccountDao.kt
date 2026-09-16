package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserAccountDao {
    @Query("SELECT * FROM user_accounts WHERE username = :username COLLATE NOCASE LIMIT 1")
    suspend fun getByUsername(username: String): UserAccount?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: UserAccount): Long

    @Query("UPDATE user_accounts SET passwordHash = :newPasswordHash WHERE username = :username COLLATE NOCASE")
    suspend fun updatePassword(username: String, newPasswordHash: String): Int
}
