package com.github.llmaximll.magicpasswords.data

import androidx.room.Entity
import androidx.room.Fts4

@Entity(tableName = "PasswordInfoFts")
@Fts4(contentEntity = PasswordInfo::class)
data class PasswordInfoFts(
    val name: String,
    val address: String,
    val password: String,
    val description: String
)