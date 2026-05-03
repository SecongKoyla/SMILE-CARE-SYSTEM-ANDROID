package com.android.smilecare.utils

import android.app.Activity
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment

fun Activity.getEditTextValue(id: Int): String {
    return findViewById<EditText>(id).text.toString().trim()
}

fun Activity.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Fragment.toast(message: String) {
    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
}

fun View.visible() { visibility = View.VISIBLE }
fun View.gone() { visibility = View.GONE }
fun View.invisible() { visibility = View.INVISIBLE }
