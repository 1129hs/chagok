package com.heiseung.chagok.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.heiseung.chagok.data.local.entity.MemberEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MemberDao {

    @Query("SELECT * FROM member WHERE isActive = 1 ORDER BY createdAt")
    fun observeActive(): Flow<List<MemberEntity>>

    @Query("SELECT * FROM member WHERE isActive = 1 ORDER BY createdAt")
    suspend fun getActive(): List<MemberEntity>

    @Query("SELECT * FROM member WHERE id = :id")
    suspend fun getById(id: Long): MemberEntity?

    @Insert
    suspend fun insert(member: MemberEntity): Long

    @Update
    suspend fun update(member: MemberEntity)

    @Query("UPDATE member SET isActive = 0 WHERE id = :id")
    suspend fun deactivate(id: Long)
}
