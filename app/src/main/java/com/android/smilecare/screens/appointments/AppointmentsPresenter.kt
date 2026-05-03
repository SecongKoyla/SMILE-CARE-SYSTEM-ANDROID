package com.android.smilecare.screens.appointments

import com.android.smilecare.app.CustomApp
import com.android.smilecare.data.Appointment
import com.android.smilecare.data.AppointmentStatus

class AppointmentsPresenter(
    private val view: AppointmentsContract.View,
    private val app: CustomApp
) : AppointmentsContract.Presenter {

    private var currentFilter = "All"

    override fun loadAppointments(filter: String) {
        currentFilter = filter
        val userEmail = app.loggedInUser?.email
        val allAppts = app.appointments
            .asSequence()
            .filter { userEmail != null && it.userEmail.equals(userEmail, ignoreCase = true) }
            .sortedByDescending { it.date }
            .toList()
        
        val filtered = if (filter == "All") {
            allAppts
        } else {
            allAppts.filter { it.status.name.equals(filter, ignoreCase = true) }
        }

        if (filtered.isEmpty()) {
            view.showEmptyState(true)
            view.showAppointments(emptyList())
        } else {
            view.showEmptyState(false)
            view.showAppointments(filtered)
        }
    }

    override fun cancelAppointment(appointment: Appointment) {
        val index = app.appointments.indexOfFirst { it.id == appointment.id }
        if (index != -1) {
            val updated = app.appointments[index].copy(status = AppointmentStatus.CANCELLED)
            app.appointments[index] = updated
            app.saveAppointments()
            view.showMessage("Appointment cancelled")
            loadAppointments(currentFilter) // reload with same filter
        }
    }
}