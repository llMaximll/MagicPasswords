package com.github.llmaximll.magicpasswords.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.github.llmaximll.magicpasswords.model.PasswordInfo
import com.github.llmaximll.magicpasswords.model.PasswordInfoFts

@Database(entities = [ PasswordInfo::class, PasswordInfoFts::class ], version = 2, exportSchema = false)
@TypeConverters(MagicTypeConverters::class)
abstract class MagicDatabase : RoomDatabase() {
    abstract fun magicDao(): MagicDao
}