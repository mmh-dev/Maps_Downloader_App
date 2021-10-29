package com.mmh.maps_downloader_app.utils

import android.content.Context
import android.view.View
import com.google.android.material.snackbar.Snackbar
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.listener.multi.BaseMultiplePermissionsListener
import java.util.*

const val PROGRESS = "Progress"

fun View.showSnackBar(message: String) {
    Snackbar.make(this, message, Snackbar.LENGTH_SHORT).show()
}

fun Context.askPermission(vararg permissions: String, callback: (Boolean) -> Unit) {
    Dexter.withContext(this)
        .withPermissions(*permissions)
        .withListener(object : BaseMultiplePermissionsListener() {
            override fun onPermissionsChecked(p0: MultiplePermissionsReport) {
                callback(p0.areAllPermissionsGranted())
            }
        }).check()
}

fun Double.round(decimals: Int = 2): Double =
    "%.${decimals}f".format(Locale.US, this).toDouble()