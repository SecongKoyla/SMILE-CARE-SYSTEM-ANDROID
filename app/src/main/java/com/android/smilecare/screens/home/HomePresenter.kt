package com.android.smilecare.screens.home

import java.text.SimpleDateFormat
import java.util.Locale

class HomePresenter(
    private val view: HomeContract.View,
    private val model: HomeModel
) : HomeContract.Presenter {

    override fun loadHome() {
        val user = model.getLoggedInUser() ?: return
        val greeting = model.getGreeting()
        view.showGreeting(user.lastName, greeting)

        val next = model.getNextAppointment()
        if (next != null) {
            val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            view.showNextAppointment(
                dateFormat.format(next.date),
                next.service.name,
                timeFormat.format(next.date)
            )
        } else {
            view.hideNextAppointment()
        }
    }

    override fun logout() {
        model.logout()
        view.navigateToLogin()
    }
}
