package com.android.smilecare.screens.login

class LoginPresenter(
    private val view: LoginContract.View,
    private val model: LoginModel
) : LoginContract.Presenter {

    override fun login(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            view.showEmptyFieldsMessage()
            return
        }
        val user = model.validateCredentials(email, password)
        if (user != null) {
            model.saveLoggedInUser(user)
            view.showSuccessMessage()
            view.showHomeScreen()
        } else {
            view.showInvalidCredentialsMessage()
        }
    }
}
