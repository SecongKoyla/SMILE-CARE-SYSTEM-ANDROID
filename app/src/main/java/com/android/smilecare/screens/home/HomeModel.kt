package com.android.smilecare.screens.home

import com.android.smilecare.app.CustomApp
import com.android.smilecare.data.Appointment
import com.android.smilecare.data.User
import java.util.Calendar

class HomeModel(private val app: CustomApp) {

    fun getLoggedInUser(): User? = app.loggedInUser

    fun getGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            hour < 12 -> "Good morning"
            hour < 17 -> "Good afternoon"
            else -> "Good evening"
        }
    }

    fun getNextAppointment(): Appointment? {
        return app.appointments
            .filter { it.date.after(java.util.Date()) }
            .minByOrNull { it.date }
    }

    fun logout() {
        app.loggedInUser = null
    }
}
