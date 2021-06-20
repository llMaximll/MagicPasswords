package com.github.llmaximll.magicpasswords.database

import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.util.*

@TypeConverters
class MagicTypeConverters {
    @TypeConverter
    fun fromUUID(uuid: UUID): String {
        return uuid.toString()
    }

    @TypeConverter
    fun toUUID(uuid: String): UUID? {
        return UUID.fromString(uuid)
    }
}