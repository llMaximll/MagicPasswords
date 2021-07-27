package com.github.llmaximll.magicpasswords.background

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.github.llmaximll.magicpasswords.repositories.MagicRepository
import java.util.*

class DeletePasswordWorker(
    appContext: Context,
    workerParameters: WorkerParameters,
)
    : Worker(appContext, workerParameters) {

    private val repository = MagicRepository.get()

    override fun doWork(): Result {
        val passwordId = inputData.getString("passwordId")
        try {
            val passwordUUID = UUID.fromString(passwordId)
            repository.deletePasswordById(passwordUUID)
        } catch (e: Exception) {
            return Result.failure()
        }

        return Result.success()
    }
}