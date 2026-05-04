package com.android.smilecare.screens.login

class LoginContract {
    interface View {
        fun showSuccessMessage()
        fun showInvalidCredentialsMessage()
        fun showEmptyFieldsMessage()
        fun showHomeScreen()
        fun showAdminScreen()
    }

    interface Presenter {
        fun login(email: String, password: String)
    }
}
