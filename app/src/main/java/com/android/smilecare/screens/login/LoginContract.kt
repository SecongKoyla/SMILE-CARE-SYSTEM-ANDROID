package com.android.smilecare.screens.login

class LoginContract {
    interface View {
        fun showSuccessMessage()
        fun showInvalidCredentialsMessage()
        fun showEmptyFieldsMessage()
        fun showHomeScreen()
    }

    interface Presenter {
        fun login(email: String, password: String)
    }
}
