package com.android.smilecare.screens.bookappointment

import com.android.smilecare.app.CustomApp
import com.android.smilecare.data.Appointment
import com.android.smilecare.data.DentalService

class BookAppointmentModel(private val app: CustomApp) {

    fun getServices(): List<DentalService> = app.services.toList()

    fun getClinicOpenDaysMon0(): BooleanArray = app.clinicOpenDays.copyOf()

    fun getClinicMorningStartMinutes(): Int = app.clinicMorningStartMinutes

    fun getClinicMorningEndMinutes(): Int = app.clinicMorningEndMinutes

    fun getClinicAfternoonStartMinutes(): Int = app.clinicAfternoonStartMinutes

    fun getClinicAfternoonEndMinutes(): Int = app.clinicAfternoonEndMinutes

    fun getClinicClosedDatesYmd(): IntArray = app.clinicClosures.map { it.dateYmd }.distinct().sorted().toIntArray()

    fun getAppointments(): List<Appointment> = app.appointments.toList()

    fun getLoggedInUserEmail(): String = app.loggedInUser?.email.orEmpty()

    fun addAppointment(appointment: Appointment) {
        app.appointments.add(appointment)
        app.saveAppointments()

        if (appointment.userEmail.isNotBlank()) {
            val dateFmt = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
            val date = dateFmt.format(appointment.date)
            val time = appointment.timeSlot
            app.addSystemUpdateForUserEmail(
                appointment.userEmail,
                "Appointment booked (Pending): ${appointment.service.name} on $date at $time"
            )
        }
    }
}
