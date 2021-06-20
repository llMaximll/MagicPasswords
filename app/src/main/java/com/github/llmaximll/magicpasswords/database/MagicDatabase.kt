package com.github.llmaximll.magicpasswords.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.github.llmaximll.magicpasswords.data.PasswordInfo

@Database(entities = [ PasswordInfo::class ], version = 1, exportSchema = false)
@TypeConverters(MagicTypeConverters::class)
abstract class MagicDatabase : RoomDatabase() {
    abstract fun magicDao(): MagicDao
}