package com.android.smilecare.screens.register

class RegisterContract {
    interface View {
        fun showEmptyFieldsMessage()
        fun showEmailAlreadyExistsMessage()
        fun showPasswordMismatchMessage()
        fun showSuccessMessage()
        fun showLoginScreen()
    }

    interface Presenter {
        fun register(firstName: String, lastName: String, email: String, password: String, confirmPassword: String)
    }
}
