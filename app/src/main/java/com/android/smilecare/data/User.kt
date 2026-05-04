package com.android.smilecare.data

data class User(
    var firstName: String = "",
    var lastName: String = "",
    var email: String = "",
    var password: String = "",
    var photoUri: String = "",
    var role: UserRole = UserRole.USER
)

enum class UserRole {
    USER, ADMIN
}
