package com.github.llmaximll.magicpasswords.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class PasswordInfo(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val name: String,
    val password: String,
    val description: String = ""
)
