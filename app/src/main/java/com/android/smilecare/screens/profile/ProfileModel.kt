package com.android.smilecare.screens.profile

import com.android.smilecare.app.CustomApp
import com.android.smilecare.data.User

class ProfileModel(private val app: CustomApp) {

    fun getLoggedInUser(): User? = app.loggedInUser

    fun updateName(firstName: String, lastName: String): Boolean {
        val user = app.loggedInUser ?: return false
        val email = user.email

        val index = app.registeredUsers.indexOfFirst { it.email.equals(email, ignoreCase = true) }
        if (index == -1) return false

        app.registeredUsers[index].firstName = firstName
        app.registeredUsers[index].lastName = lastName

        // Keep session user in sync
        app.loggedInUser?.firstName = firstName
        app.loggedInUser?.lastName = lastName

        app.saveUsers()
        return true
    }

    enum class PasswordUpdateResult {
        SUCCESS,
        EMPTY_FIELDS,
        CURRENT_INCORRECT,
        MISMATCH,
        TOO_SHORT,
        NOT_LOGGED_IN
    }

    fun updatePassword(currentPassword: String, newPassword: String, confirmPassword: String): PasswordUpdateResult {
        val user = app.loggedInUser ?: return PasswordUpdateResult.NOT_LOGGED_IN

        if (currentPassword.isBlank() || newPassword.isBlank() || confirmPassword.isBlank()) {
            return PasswordUpdateResult.EMPTY_FIELDS
        }

        val email = user.email
        val index = app.registeredUsers.indexOfFirst { it.email.equals(email, ignoreCase = true) }
        if (index == -1) return PasswordUpdateResult.NOT_LOGGED_IN

        val existing = app.registeredUsers[index]
        if (existing.password != currentPassword) return PasswordUpdateResult.CURRENT_INCORRECT

        if (newPassword.length < 8) return PasswordUpdateResult.TOO_SHORT
        if (newPassword != confirmPassword) return PasswordUpdateResult.MISMATCH

        app.registeredUsers[index].password = newPassword
        app.loggedInUser?.password = newPassword
        app.saveUsers()

        return PasswordUpdateResult.SUCCESS
    }

    fun setPhotoUri(uriString: String) {
        val user = app.loggedInUser ?: return
        val email = user.email

        val index = app.registeredUsers.indexOfFirst { it.email.equals(email, ignoreCase = true) }
        if (index == -1) return

        app.registeredUsers[index].photoUri = uriString
        app.loggedInUser?.photoUri = uriString
        app.saveUsers()
    }

    fun removePhoto() {
        setPhotoUri("")
    }
}
