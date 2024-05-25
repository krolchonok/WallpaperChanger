package com.ushastoe.wallpaperchanger

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

class ShareActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_share)

        val intent = intent
        if (Intent.ACTION_SEND == intent.action && intent.type != null) {
            println(intent.type)
            val imageUri: Uri? = if (android.os.Build.VERSION.SDK_INT >= 33) {
                intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
            }
            if (imageUri != null) {
                copyImageToFolder(imageUri)
            }
        }
        finish()
    }
    private fun copyImageToFolder(uri: Uri) {
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        val targetDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "Wallpapers")
        if (!targetDir.exists()) {
            targetDir.mkdirs()
        }
        val fileName = generateRandomFileName()
        val targetFile = File(targetDir, "$fileName.jpg")
        val outputStream: OutputStream = FileOutputStream(targetFile)

        try {
            inputStream?.let {
                copyStream(it, outputStream)
            }
            Toast.makeText(this, getString(R.string.saved) + "\u00A0" + fileName + ".jpg", Toast.LENGTH_LONG).show()

        } finally {
            inputStream?.close()
            outputStream.close()
        }
    }
    private fun copyStream(input: InputStream, output: OutputStream) {
        val buffer = ByteArray(1024)
        var bytesRead: Int
        while (input.read(buffer).also { bytesRead = it } != -1) {
            output.write(buffer, 0, bytesRead)
        }
    }

    private fun generateRandomFileName(): String {
        val source = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..8)
            .map { source.random() }
            .joinToString("")
    }
}