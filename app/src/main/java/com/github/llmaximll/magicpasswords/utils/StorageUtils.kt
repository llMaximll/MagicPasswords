package com.github.llmaximll.magicpasswords.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.IOException

object StorageUtils {
    private fun createOrGetFile(
        destination: File,
        fileName: String,
        folderName: String
    ): File {
        val folder = File(destination, folderName)
        return File(folder, fileName)
    }

    private fun readFile(
        context: Context,
        file: File
    ): String {
        val sb = StringBuilder()
        if (file.exists()) {
            try {
                val bufferedReader = file.bufferedReader()
                bufferedReader.useLines { lines ->
                    lines.forEach {
                        sb.append(it)
                        sb.append("\n")
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return sb.toString()
    }

    private fun writeFile(
        context: Context,
        text: String,
        file: File
    ) {
        try {
            file.parentFile?.mkdirs()
            file.bufferedWriter().use { out ->
                out.write(text)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun isExternalStorageWritable(): Boolean {
        val state = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED == state
    }

    fun isExternalStorageReadable(): Boolean {
        val state = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED == state ||
                Environment.MEDIA_MOUNTED_READ_ONLY == state
    }

    fun getTextFromStorage(
        rootDestination: File = File("/storage/emulated/0/"),
        context: Context,
        fileName: String?,
        folderName: String?
    ): String {
        val file = if (fileName != null && folderName != null) {
            createOrGetFile(rootDestination, fileName, folderName)
        } else {
            File(rootDestination.path)
        }
        return readFile(context, file)
    }

    fun setTextInStorage(
        rootDestination: File = File("/storage/emulated/0/"),
        context: Context,
        fileName: String?,
        folderName: String?,
        text: String
    ) {
        val file = if (fileName != null && folderName != null) {
            createOrGetFile(rootDestination, fileName, folderName)
        } else {
            File(rootDestination.path)
        }
        writeFile(context, text, file)
    }

    fun checkSelfPermission(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    fun requestPermissions(
        activity: Activity,
        permissionsArray: Array<String>,
        requestCode: Int
    ) {
        ActivityCompat.requestPermissions(
            activity,
            permissionsArray,
            requestCode
        )
    }
}