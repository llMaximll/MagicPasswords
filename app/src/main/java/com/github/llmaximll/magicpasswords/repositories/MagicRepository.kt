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

    suspend fun getAllPasswords(removed: Int = 0): List<PasswordInfo> =
        magicDao.getAllPasswords(removed)

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

    fun deletePasswordById(passwordId: UUID) {
        magicDao.deletePasswordById(passwordId)
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