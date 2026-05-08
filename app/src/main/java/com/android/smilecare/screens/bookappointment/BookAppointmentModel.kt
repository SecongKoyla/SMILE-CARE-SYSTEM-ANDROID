package com.android.smilecare.screens.bookappointment

import com.android.smilecare.app.CustomApp
import com.android.smilecare.data.Appointment
import com.android.smilecare.data.DentalService

class BookAppointmentModel(private val app: CustomApp) {

    fun getServices(): List<DentalService> = app.services.toList()

    fun getClinicOpenDaysMon0(): BooleanArray = app.clinicOpenDays.copyOf()

    fun getClinicOpeningMinutes(): Int = app.clinicOpeningMinutes

    fun getClinicClosingMinutes(): Int = app.clinicClosingMinutes

    fun getAppointments(): List<Appointment> = app.appointments.toList()

    fun getLoggedInUserEmail(): String = app.loggedInUser?.email.orEmpty()

    fun addAppointment(appointment: Appointment) {
        app.appointments.add(appointment)
        app.saveAppointments()
    }
}
