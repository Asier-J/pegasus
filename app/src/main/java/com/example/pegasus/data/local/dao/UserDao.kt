package com.example.pegasus.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.pegasus.domain.User
import kotlinx.coroutines.flow.Flow

/**
 * Sprint 03: DAO for the User profile mirror.
 * Inserts use ABORT so we surface duplicate-username errors at the repository.
 */
@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: User)

    @Update
    suspend fun update(user: User)

    @Query("SELECT * FROM users WHERE uid = :uid LIMIT 1")
    suspend fun getByUid(uid: String): User?

    @Query("SELECT * FROM users WHERE uid = :uid LIMIT 1")
    fun observeByUid(uid: String): Flow<User?>

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun findByUsername(username: String): User?

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE username = :username AND uid != :excludeUid)")
    suspend fun isUsernameTakenByOther(username: String, excludeUid: String): Boolean

    @Query("DELETE FROM users WHERE uid = :uid")
    suspend fun delete(uid: String)
}
