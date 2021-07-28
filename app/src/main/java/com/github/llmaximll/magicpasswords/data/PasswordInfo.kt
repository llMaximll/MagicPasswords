package com.github.llmaximll.magicpasswords.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.llmaximll.magicpasswords.utils.Common
import java.util.*

@Entity
data class PasswordInfo(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val name: String,
    val address: String,
    val password: String,
    val description: String = "",
    var removed: Int = 0,
    var removedDate: Long = 0L,
    var removedFormat: Int = Common.TimeDeleteMonthSP,
    var messagePassword: Boolean = false
)