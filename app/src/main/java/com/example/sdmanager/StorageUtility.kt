package com.example.sdmanager

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.math.log10
import kotlin.math.pow

object StorageUtility {
    fun showFiles(root: File, view: View, navController: NavController, msg: String, activity: Context) {
        val filesList = root.listFiles()!!.map {
            FileModel(
                it.name,
                it.path,
                it.listFiles()?.size ?: 0,
                it.isDirectory,
                Instant.ofEpochMilli(it.lastModified()).atZone(
                    ZoneId.ofOffset("UTC", ZoneOffset.ofHours(7))
                ),
                it.length()
            )
        }
        val fileAdapter = FileAdapter(
            filesList.toMutableList(),
            navigateNotFound = { name ->
                val bundle = bundleOf(
                    "msg" to msg,
                    "returnable" to true,
                    "title" to name
                )
                navController.navigate(R.id.not_found, bundle)
            },
            navigateDown = { name ->
                val bundle = bundleOf("path" to name)
                navController.navigate(R.id.open_folder, bundle)
            },
            context = activity
        )
        view.findViewById<RecyclerView>(R.id.file_list)?.run {
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            adapter = fileAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    fun fileSizeConversion(size: Long): Pair<Double, String> {
        if (size <= 0) return Pair(0.0, "B")
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
        return Pair(
            size / 1024.0.pow(digitGroups.toDouble()),
            units[digitGroups]
        )
    }

    fun checkStorageExists(): Boolean {
        val state = Environment.getExternalStorageState()
        return state == Environment.MEDIA_MOUNTED
    }

    private val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) arrayOf(
        Manifest.permission.READ_MEDIA_AUDIO,
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.READ_MEDIA_VIDEO,
        Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
    ) else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) arrayOf(
        Manifest.permission.READ_MEDIA_AUDIO,
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.READ_MEDIA_VIDEO,
    ) else arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    private fun requestPermissionIfNeeded(
        context: Context,
        launcher: ActivityResultLauncher<Array<String>>
    ) {
        val deniedPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()
        if (deniedPermissions.isNotEmpty()) {
            launcher.launch(deniedPermissions)
        }
    }

    fun checkStoragePermissionGranted(
        context: Context,
        launcher: ActivityResultLauncher<Array<String>>
    ): Boolean {
        val permissions = permissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
        if (permissions.isNotEmpty()) {
            requestPermissionIfNeeded(context, launcher)
        }
        return permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun openFile(context: Context, path: String) {
        val file = File(path)
        val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)

        // Determine the MIME type of the file
        val mimeType = context.contentResolver.getType(uri) ?: "*/*"

        // Create an intent to open the file
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Grant read permissions
        }

        // Check if there's an app that can handle the file type
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            Toast.makeText(context, context.getString(R.string.not_open), Toast.LENGTH_LONG).show()
        }
    }

}