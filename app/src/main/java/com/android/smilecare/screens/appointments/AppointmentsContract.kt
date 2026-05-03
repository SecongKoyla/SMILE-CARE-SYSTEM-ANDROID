package com.android.smilecare.screens.appointments

import com.android.smilecare.data.Appointment

interface AppointmentsContract {
    interface View {
        fun showAppointments(appointments: List<Appointment>)
        fun showEmptyState(show: Boolean)
        fun showMessage(msg: String)
    }

    interface Presenter {
        fun loadAppointments(filter: String = "All")
        fun cancelAppointment(appointment: Appointment)
    }
}