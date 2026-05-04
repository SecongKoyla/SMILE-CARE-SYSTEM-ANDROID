package com.android.smilecare.screens.profile

class ProfilePresenter(
    private val view: ProfileContract.View,
    private val model: ProfileModel
) : ProfileContract.Presenter {

    override fun load() {
        val user = model.getLoggedInUser()
        if (user == null) {
            view.showMessage("Please login again.")
            view.closeScreen()
            return
        }
        view.showUser(user)
    }

    override fun onSavePersonalInfo(firstName: String, lastName: String) {
        if (firstName.isBlank() || lastName.isBlank()) {
            view.showMessage("Please fill in first and last name.")
            return
        }
        val ok = model.updateName(firstName.trim(), lastName.trim())
        if (ok) {
            view.showMessage("Changes saved.")
            load()
        } else {
            view.showMessage("Unable to save changes.")
        }
    }

    override fun onUpdatePassword(currentPassword: String, newPassword: String, confirmPassword: String) {
        when (model.updatePassword(currentPassword, newPassword, confirmPassword)) {
            ProfileModel.PasswordUpdateResult.SUCCESS -> view.showMessage("Password updated.")
            ProfileModel.PasswordUpdateResult.EMPTY_FIELDS -> view.showMessage("Please fill in all password fields.")
            ProfileModel.PasswordUpdateResult.CURRENT_INCORRECT -> view.showMessage("Current password is incorrect.")
            ProfileModel.PasswordUpdateResult.MISMATCH -> view.showMessage("New passwords do not match.")
            ProfileModel.PasswordUpdateResult.TOO_SHORT -> view.showMessage("New password must be at least 8 characters.")
            ProfileModel.PasswordUpdateResult.NOT_LOGGED_IN -> {
                view.showMessage("Please login again.")
                view.closeScreen()
            }
        }
    }

    override fun onRemovePhoto() {
        model.removePhoto()
        load()
        view.showMessage("Photo removed.")
    }

    override fun onSetPhoto(uriString: String) {
        model.setPhotoUri(uriString)
        load()
        view.showMessage("Photo updated.")
    }
}
