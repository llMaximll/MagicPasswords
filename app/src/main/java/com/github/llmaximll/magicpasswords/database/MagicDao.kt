package com.github.llmaximll.magicpasswords.database

import androidx.room.*
import com.github.llmaximll.magicpasswords.data.PasswordInfo
import com.github.llmaximll.magicpasswords.data.PasswordInfoFts
import java.util.*

@Dao
interface MagicDao {
    @Query("SELECT * FROM PasswordInfo WHERE removed=(:removed)")
    suspend fun getAllPasswords(removed: Int = 0): List<PasswordInfo>

    @Query("SELECT * FROM PasswordInfo WHERE id=(:idPassword)")
    suspend fun getPasswordInfo(idPassword: UUID): PasswordInfo

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addPassword(passwordInfo: PasswordInfo)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addAllPasswords(passwordsList: List<PasswordInfo>)

    @Update
    fun updatePassword(passwordInfo: PasswordInfo)

    @Update
    fun updateAllPasswords(passwordInfoList: List<PasswordInfo>): Int

    @Delete
    fun deleteAllPasswords(passwordInfoList: List<PasswordInfo>): Int
    
    @Query("DELETE FROM PasswordInfo WHERE id=(:passwordId)")
    fun deletePasswordById(passwordId: UUID)

//    @Query("SELECT * FROM PasswordInfoFts WHERE name LIKE ('%' + :text + '%') OR address LIKE ('%' + :text + '%') OR description LIKE ('%' + :text + '%')")
//    fun getRelevantPasswords(text: String): List<PasswordInfo>

    @Query("""
        SELECT *
        FROM PasswordInfo
        JOIN PasswordInfoFts ON PasswordInfo.name = PasswordInfoFts.name
        WHERE PasswordInfoFts MATCH :query
    """)
    suspend fun getRelevantPasswords(query: String): List<PasswordInfo>
}