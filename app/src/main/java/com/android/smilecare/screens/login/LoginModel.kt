package com.android.smilecare.screens.login

import com.android.smilecare.app.CustomApp
import com.android.smilecare.data.User

class LoginModel(private val app: CustomApp) {

    fun validateCredentials(email: String, password: String): User? {
        return app.registeredUsers.find {
            it.email.equals(email, ignoreCase = true) && it.password == password
        }
    }

    fun saveLoggedInUser(user: User) {
        app.loggedInUser = user
    }
}
