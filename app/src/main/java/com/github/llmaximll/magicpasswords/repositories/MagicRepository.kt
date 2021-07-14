package com.github.llmaximll.magicpasswords.repositories

import android.content.Context
import androidx.room.Room
import com.github.llmaximll.magicpasswords.common.CommonFunctions
import com.github.llmaximll.magicpasswords.data.PasswordInfo
import com.github.llmaximll.magicpasswords.database.MagicDatabase
import com.github.llmaximll.magicpasswords.fragments.ChangePasswordFragment
import java.util.*

private const val DATABASE_NAME = "MagicDatabase"

class MagicRepository private constructor(context: Context) {

    private val database: MagicDatabase = Room.databaseBuilder(
        context.applicationContext,
        MagicDatabase::class.java,
        DATABASE_NAME
    ).build()

    private val magicDao = database.magicDao()

    fun generateSecretKey(context: Context) {
        val cf = CommonFunctions.get()
        val sp = cf.getSharedPreferences(context)
        val secretKey: String? = sp.getString(cf.spSecretKey, null)
        if (secretKey == null) {
            val editor = sp.edit()
            editor.putString(
                cf.spSecretKey,
                generatePassword(45, ChangePasswordFragment.PASSWORD_FORMAT_WITH_SPEC_SYMBOLS)
            )
            editor.apply()
        }
    }

    fun generatePassword(
        count: Int,
        passwordFormat: Int = ChangePasswordFragment.PASSWORD_FORMAT_WITHOUT_SPEC_SYMBOLS
    ): String {
        val dict = when (passwordFormat) {
            ChangePasswordFragment.PASSWORD_FORMAT_WITHOUT_SPEC_SYMBOLS -> {
                "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
            }
            ChangePasswordFragment.PASSWORD_FORMAT_WITH_SPEC_SYMBOLS -> {
                "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ\\/^&%$#@_-"
            } else -> {
                "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
            }
        }
        val rnd = Random()
        val password = StringBuilder()
        for (i in 0..count) {
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
    }
}