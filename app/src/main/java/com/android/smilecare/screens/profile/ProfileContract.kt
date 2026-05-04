package com.android.smilecare.screens.profile

import com.android.smilecare.data.User

interface ProfileContract {

    interface View {
        fun showUser(user: User)
        fun showMessage(message: String)
        fun closeScreen()
        fun showChangePasswordExpanded(expanded: Boolean)
        fun showPersonalInfoExpanded(expanded: Boolean)
    }

    interface Presenter {
        fun load()
        fun onSavePersonalInfo(firstName: String, lastName: String)
        fun onUpdatePassword(currentPassword: String, newPassword: String, confirmPassword: String)
        fun onRemovePhoto()
        fun onSetPhoto(uriString: String)
    }
}
