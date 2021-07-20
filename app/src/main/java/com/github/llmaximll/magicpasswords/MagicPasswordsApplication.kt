package com.github.llmaximll.magicpasswords

import android.app.Application
import com.github.llmaximll.magicpasswords.utils.CommonFunctions
import com.github.llmaximll.magicpasswords.repositories.MagicRepository

class MagicPasswordsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        CommonFunctions.init()
        MagicRepository.initialize(this)
        val repository = MagicRepository.get()
        repository.generateSecretKey(this)
    }
}