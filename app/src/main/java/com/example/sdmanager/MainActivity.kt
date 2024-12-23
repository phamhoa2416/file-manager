package com.example.sdmanager

import android.content.SharedPreferences
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private lateinit var myPref: SharedPreferences
    private var permissionDeniedCount = 0
    private lateinit var navController: NavController
    private val scope = CoroutineScope(Dispatchers.Main)

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.filter { it.value }.map { it.key }
        val denied = permissions.entries.filter { !it.value }.map { it.key }
        if (granted.size == permissions.size) {
            scope.launch {
                navigateNotFound(getString(R.string.granted), false)
                delay(1500)
                navController.popBackStack(navController.graph.startDestinationId, true)
                navController.navigate(R.id.rootFragment)
            }
        }
        if (denied.isNotEmpty()) {
            permissionDeniedCount++
            if (permissionDeniedCount < 2) showRationale(permissions.keys.toTypedArray())
            else {
                navigateNotFound(getString(R.string.denied), false)
            }
            myPref.edit().putInt("permissionDeniedCount",
                if (permissionDeniedCount > 2) 2 else permissionDeniedCount
            ).apply()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        myPref = getSharedPreferences("myPref", MODE_PRIVATE)
        permissionDeniedCount = myPref.getInt("permissionDeniedCount", 0)
        Log.d("Permission", "Permission denied count: $permissionDeniedCount")
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setSupportActionBar(findViewById(R.id.toolbar))
        navController = (supportFragmentManager.findFragmentById(R.id.fragment_view) as NavHostFragment).navController

        if (!StorageUtility.checkStorageExists()) {
            navigateNotFound(getString(R.string.not_available), false)
            return
        }

        if (StorageUtility.checkStoragePermissionGranted(this, permissionLauncher)) {
            val root = Environment.getExternalStorageDirectory()
            if (root.listFiles().isNullOrEmpty()) {
                navigateNotFound(getString(R.string.no_files_found))
                return
            }
            navController.popBackStack(navController.graph.startDestinationId, true)
            navController.navigate(R.id.rootFragment)
        } else {
            navigateNotFound(getString(R.string.denied), false)
        }
    }

    private fun showRationale(
        permissions: Array<String>
    ) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_title))
            .setMessage(getString(R.string.dialog_message))
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                permissionLauncher.launch(permissions)
            }.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }.create().apply {
                setCanceledOnTouchOutside(false)
            }.show()
    }

    private fun navigateNotFound(message: String, returnable: Boolean = true) {
        val bundle = bundleOf(
            "msg" to message,
            "returnable" to returnable
        )
        navController.popBackStack(navController.graph.startDestinationId, true)
        navController.navigate(
            R.id.notFound,
            bundle
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                navController.popBackStack()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}