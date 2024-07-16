package com.example.hamsterkombatbot.other

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import okhttp3.logging.HttpLoggingInterceptor

class OkHttpFileLogger(val name: String, val context: Context) : HttpLoggingInterceptor.Logger {
    private val file = File(context.filesDir, name)
    private val maxFileSize = 10 * 1024 * 1024 // 10MB in bytes

    override fun log(message: String) {
        if (file.exists() && file.length() > maxFileSize) {
            handleFileOverflow()
        }
        writeToLogFile(message)
    }

    private fun handleFileOverflow() {
        // Option 1: Clear the file content
        file.writeText("")

        // Option 2: Delete the file and create a new one
        // file.delete()
        // file.createNewFile()

        // Option 3: Archive the file and create a new one (not implemented here)
    }

    private fun writeToLogFile(message: String) {
        FileOutputStream(file, true).use { fos ->
            OutputStreamWriter(fos).use { writer ->
                writer.append(message)
                writer.append("\n")
            }
        }
    }
}
