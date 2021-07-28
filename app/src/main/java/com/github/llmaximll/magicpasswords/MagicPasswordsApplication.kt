package com.github.llmaximll.magicpasswords

import android.app.Application
import com.github.llmaximll.magicpasswords.repositories.MagicRepository

class MagicPasswordsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MagicRepository.initialize(this)
        val repository = MagicRepository.get()
        repository.generateSecretKey(this)
    }
}