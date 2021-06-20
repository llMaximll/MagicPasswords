package com.github.llmaximll.magicpasswords.repositories

import android.content.Context
import androidx.room.Room
import com.github.llmaximll.magicpasswords.data.PasswordInfo
import com.github.llmaximll.magicpasswords.database.MagicDatabase
import java.util.*

private const val DATABASE_NAME = "MagicDatabase"

class MagicRepository private constructor(context: Context) {

    private val database: MagicDatabase = Room.databaseBuilder(
        context.applicationContext,
        MagicDatabase::class.java,
        DATABASE_NAME
    ).build()

    private val magicDao = database.magicDao()

    suspend fun getAllPasswords(): List<PasswordInfo> =
        magicDao.getAllPasswords()

    suspend fun getPasswordInfo(idPassword: UUID): PasswordInfo =
        magicDao.getPasswordInfo(idPassword)

    fun addPassword(passwordInfo: PasswordInfo) {
        magicDao.addPassword(passwordInfo)
    }

    fun updatePassword(passwordInfo: PasswordInfo) {
        magicDao.updatePassword(passwordInfo)
    }

    fun deletePassword(passwordInfo: PasswordInfo) {
        magicDao.deletePassword(passwordInfo)
    }

    companion object {
        private var INSTANCE: MagicRepository? = null
        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = MagicRepository(context)
            }
        }
        fun get(): MagicRepository {
            return checkNotNull(INSTANCE) {
                "MagicRepository must be initialized"
            }
        }
    }
}