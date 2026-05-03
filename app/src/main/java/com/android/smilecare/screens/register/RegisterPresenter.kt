package com.android.smilecare.screens.register

class RegisterPresenter(
    private val view: RegisterContract.View,
    private val model: RegisterModel
) : RegisterContract.Presenter {

    override fun register(
        firstName: String, lastName: String,
        email: String, password: String, confirmPassword: String
    ) {
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() ||
            password.isEmpty() || confirmPassword.isEmpty()
        ) {
            view.showEmptyFieldsMessage()
            return
        }
        if (password != confirmPassword) {
            view.showPasswordMismatchMessage()
            return
        }
        if (password.length < 8) {
            view.showPasswordLengthMessage()
            return
        }
        if (model.isEmailTaken(email)) {
            view.showEmailAlreadyExistsMessage()
            return
        }
        model.registerUser(firstName, lastName, email, password)
        view.showSuccessMessage()
        view.showLoginScreen()
    }
}
