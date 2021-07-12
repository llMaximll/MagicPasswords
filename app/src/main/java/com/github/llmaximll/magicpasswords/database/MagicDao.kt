package com.github.llmaximll.magicpasswords.database

import androidx.room.*
import com.github.llmaximll.magicpasswords.data.PasswordInfo
import java.util.*

@Dao
interface MagicDao {
    @Query("SELECT * FROM PasswordInfo WHERE removed=(:removed)")
    suspend fun getAllPasswords(removed: Int = 0): List<PasswordInfo>

    @Query("SELECT * FROM PasswordInfo WHERE id=(:idPassword)")
    suspend fun getPasswordInfo(idPassword: UUID): PasswordInfo

    @Insert
    fun addPassword(passwordInfo: PasswordInfo)

    @Update
    fun updatePassword(passwordInfo: PasswordInfo)

    @Update
    fun updateAllPasswords(passwordInfoList: List<PasswordInfo>): Int

    @Delete
    fun deleteAllPasswords(passwordInfoList: List<PasswordInfo>): Int
    
    @Query("DELETE FROM PasswordInfo WHERE id=(:passwordId)")
    fun deletePasswordById(passwordId: UUID)
}