package com.android.smilecare.data

data class User(
    var firstName: String = "",
    var lastName: String = "",
    var email: String = "",
    var password: String = "",
    var photoUri: String = "",
    var role: UserRole = UserRole.USER,
    var id: String = java.util.UUID.randomUUID().toString()
)

enum class UserRole {
    USER, ADMIN
}
