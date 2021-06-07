package com.bobby.downloadManager.utils

import android.content.Context
import android.widget.Toast

object HelperExtensions {
    fun Context.toastShort(message: CharSequence) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
    }
}