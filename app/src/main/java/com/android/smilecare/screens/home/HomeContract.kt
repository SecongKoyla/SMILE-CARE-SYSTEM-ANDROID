package com.android.smilecare.screens.home

class HomeContract {
    interface View {
        fun showGreeting(name: String, greeting: String)
        fun showNextAppointment(date: String, service: String, time: String)
        fun hideNextAppointment()
        fun navigateToLogin()
    }

    interface Presenter {
        fun loadHome()
        fun logout()
    }
}
