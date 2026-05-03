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
        val allAppts = app.appointments.sortedByDescending { it.date }
        
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
        val index = app.appointments.indexOf(appointment)
        if (index != -1) {
            val updated = app.appointments[index].copy(status = AppointmentStatus.CANCELLED)
            app.appointments[index] = updated
            view.showMessage("Appointment cancelled")
            loadAppointments(currentFilter) // reload with same filter
        }
    }
}