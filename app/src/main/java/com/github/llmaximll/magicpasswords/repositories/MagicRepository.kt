package com.github.llmaximll.magicpasswords.repositories

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.github.llmaximll.magicpasswords.data.PasswordInfo
import com.github.llmaximll.magicpasswords.database.MagicDatabase
import com.github.llmaximll.magicpasswords.utils.Common
import java.util.*

class MagicRepository private constructor(context: Context) {

    private val database: MagicDatabase = Room.databaseBuilder(
        context.applicationContext,
        MagicDatabase::class.java,
        DATABASE_NAME
    ).apply {
        addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                db.execSQL("INSERT INTO PasswordInfoFts(PasswordsInfoFts) VALUES ('rebuild')")
            }
        })
    }.build()

    private val magicDao = database.magicDao()

    fun generateSecretKey(context: Context) {
        val sp = Common.getSharedPreferences(context)
        val secretKey: String? = sp.getString(Common.spSecretKey, null)
        if (secretKey == null) {
            val editor = sp.edit()
            editor.putString(
                Common.spSecretKey,
                generatePassword()
            )
            editor.apply()
        }
    }

    private fun generatePassword(): String {
        val dict = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ\\/^&%$#@_-"
        val rnd = Random()
        val password = StringBuilder()
        for (i in 0..45) {
            password.append(dict[rnd.nextInt(dict.length)])
        }
        return password.toString()
    }

    suspend fun getAllPasswords(removed: Int = 0): List<PasswordInfo> =
        magicDao.getAllPasswords(removed)

    suspend fun getPasswordInfo(idPassword: UUID): PasswordInfo =
        magicDao.getPasswordInfo(idPassword)

    fun addPassword(passwordInfo: PasswordInfo) {
        magicDao.addPassword(passwordInfo)
    }

    fun addAllPasswords(passwordsList: List<PasswordInfo>) {
        magicDao.addAllPasswords(passwordsList)
    }

    fun updatePassword(passwordInfo: PasswordInfo) {
        magicDao.updatePassword(passwordInfo)
    }

    fun updateAllPasswords(passwordInfoList: List<PasswordInfo>): Int {
        return magicDao.updateAllPasswords(passwordInfoList)
    }

    fun deleteAllPasswords(passwordInfoList: List<PasswordInfo>): Int {
        return magicDao.deleteAllPasswords(passwordInfoList)
    }

    fun deletePasswordById(passwordId: UUID) {
        magicDao.deletePasswordById(passwordId)
    }

    suspend fun getRelevantPasswords(query: String): List<PasswordInfo> {
        return magicDao.getRelevantPasswords(query)
    }

    fun clearAllDatabase() {
        database.clearAllTables()
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
        private const val DATABASE_NAME = "MagicDatabase"
    }
}