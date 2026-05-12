package com.android.smilecare.utils

import android.app.Activity
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment

fun Activity.getEditTextValue(id: Int): String {
    return findViewById<EditText>(id)?.text?.toString()?.trim().orEmpty()
}

fun Activity.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Activity.disableEdgeToEdge() {
    val decorView = window.decorView
    decorView.setOnApplyWindowInsetsListener { v, insets ->
        val left = insets.systemWindowInsetLeft
        val top = insets.systemWindowInsetTop
        val right = insets.systemWindowInsetRight
        val bottom = insets.systemWindowInsetBottom
        v.setPadding(left, top, right, bottom)
        insets.consumeSystemWindowInsets()
    }
}

fun Fragment.toast(message: String) {
    val ctx = context ?: return
    Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show()
}

fun View.visible() { visibility = View.VISIBLE }
fun View.gone() { visibility = View.GONE }
fun View.invisible() { visibility = View.INVISIBLE }
