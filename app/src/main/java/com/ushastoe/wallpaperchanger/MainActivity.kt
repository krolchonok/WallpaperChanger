package com.ushastoe.wallpaperchanger

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.color.DynamicColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Paths


class MainActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DynamicColors.applyToActivityIfAvailable(this@MainActivity)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        setContentView(R.layout.activity_main)

        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                copyImageToFolder(uri)
            }
        }

        findViewById<FloatingActionButton>(R.id.addButton).setOnClickListener{
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        findViewById<FloatingActionButton>(R.id.addButton).setOnLongClickListener{
            val builder = MaterialAlertDialogBuilder(this)
            builder.setMessage(R.string.about)
                .setCancelable(false)
                .setNegativeButton(R.string.copy) { dialog, _ ->
                    val clipData = ClipData.newPlainText("label", "/storage/emulated/0/Documents/Wallpapers")
                    clipboardManager.setPrimaryClip(clipData)
                    Toast.makeText(this, R.string.copy_success, Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                .setNeutralButton(R.string.gh) { dialog, _ ->
                    val url = "https://github.com/krolchonok/WallpaperChanger"
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(url)
                    startActivity(intent)
                    dialog.dismiss()
                }
                .setPositiveButton(R.string.close) { dialog, _ ->
                    dialog.dismiss()
                }
            val alert = builder.create()
            alert.show()
            true
        }

        val topAppBar: MaterialToolbar = findViewById(R.id.topAppBar)
        topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.update -> {
                    start()
                    true
                }
                else -> false
            }
        }

        if (!Environment.isExternalStorageManager()) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            val uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            startActivity(intent)
        }
        start()
    }

    private fun start() {
        val recyclerView: RecyclerView = findViewById(R.id.RecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = CustomRecyclerAdapter(fillList(), this)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
    }
    private fun fillList(): MutableList<String> {
        val directoryPath = "/storage/emulated/0/Documents/Wallpapers"
        val directFile = File(directoryPath)

        val data = mutableListOf<String>()

        if (!directFile.isDirectory) {
            Files.createDirectory(Paths.get(directoryPath))
            Toast.makeText(this, R.string.ready_start, Toast.LENGTH_SHORT).show()
        } else {
            if (directFile.listFiles()?.isEmpty() == false) {
                directFile.listFiles()?.filter { it.isFile }?.forEach { file ->
                    if ((".jpg" in file.name) or (".png" in file.name)) {
                        data.add("$directoryPath/${file.name}")
                    }
                }
            }
        }
        return data
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
        } finally {
            inputStream?.close()
            outputStream.close()
        }
    }

    // Service function
    override fun onPause() {
        super.onPause()
        start()
    }
    override fun onResume() {
        super.onResume()
        start()
    }
    private fun generateRandomFileName(): String {
        val source = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..8)
            .map { source.random() }
            .joinToString("")
    }
    private fun copyStream(input: InputStream, output: OutputStream) {
        val buffer = ByteArray(1024)
        var bytesRead: Int
        while (input.read(buffer).also { bytesRead = it } != -1) {
            output.write(buffer, 0, bytesRead)
        }
    }
}
