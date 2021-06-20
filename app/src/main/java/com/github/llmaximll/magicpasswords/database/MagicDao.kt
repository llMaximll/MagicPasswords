package com.github.llmaximll.magicpasswords.database

import androidx.room.*
import com.github.llmaximll.magicpasswords.data.PasswordInfo
import java.util.*

@Dao
interface MagicDao {

    @Query("SELECT * FROM PasswordInfo")
    suspend fun getAllPasswords(): List<PasswordInfo>

    @Query("SELECT * FROM PasswordInfo WHERE id=(:idPassword)")
    suspend fun getPasswordInfo(idPassword: UUID): PasswordInfo

    @Insert
    fun addPassword(passwordInfo: PasswordInfo)

    @Update
    fun updatePassword(passwordInfo: PasswordInfo)

    @Delete
    fun deletePassword(passwordInfo: PasswordInfo)
}