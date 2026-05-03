package com.android.smilecare.screens.register

import com.android.smilecare.app.CustomApp
import com.android.smilecare.data.User

class RegisterModel(private val app: CustomApp) {

    fun isEmailTaken(email: String): Boolean {
        return app.registeredUsers.any { it.email.equals(email, ignoreCase = true) }
    }

    fun registerUser(firstName: String, lastName: String, email: String, password: String) {
        app.registeredUsers.add(User(firstName, lastName, email, password))
        app.saveUsers()
    }
}
