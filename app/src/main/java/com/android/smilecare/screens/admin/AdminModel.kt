package com.android.smilecare.screens.admin

import com.android.smilecare.app.CustomApp
import com.android.smilecare.data.Appointment
import com.android.smilecare.data.AppointmentStatus
import com.android.smilecare.data.DentalService
import com.android.smilecare.data.User
import com.android.smilecare.data.UserRole
import java.util.Calendar
import java.util.Date

class AdminModel(private val app: CustomApp) {

    // ── Appointments ─────────────────────────────────────────────────────────

    fun getAllAppointments(): List<Appointment> =
        app.appointments.sortedByDescending { it.date }

    fun getAppointmentsByStatus(status: String): List<Appointment> {
        if (status.equals("All", ignoreCase = true)) return getAllAppointments()
        return app.appointments
            .filter { it.status.name.equals(status, ignoreCase = true) }
            .sortedByDescending { it.date }
    }

    fun getAppointmentsByDate(date: Date): List<Appointment> {
        val cal = Calendar.getInstance()
        return app.appointments.filter {
            cal.time = it.date
            val y = cal.get(Calendar.YEAR); val d = cal.get(Calendar.DAY_OF_YEAR)
            cal.time = date
            cal.get(Calendar.YEAR) == y && cal.get(Calendar.DAY_OF_YEAR) == d
        }.sortedByDescending { it.date }
    }

    fun updateStatus(appointmentId: String, newStatus: AppointmentStatus) {
        val idx = app.appointments.indexOfFirst { it.id == appointmentId }
        if (idx != -1) {
            app.appointments[idx] = app.appointments[idx].copy(status = newStatus)
            app.saveAppointments()
        }
    }

    fun deleteAppointment(appointmentId: String) {
        app.appointments.removeAll { it.id == appointmentId }
        app.saveAppointments()
    }

    // ── Services ─────────────────────────────────────────────────────────────

    fun getServices(): List<DentalService> = app.services.toList()

    fun addService(service: DentalService) {
        app.services.add(service)
        app.saveServices()
    }

    fun deleteService(service: DentalService) {
        app.services.removeAll { it.name.equals(service.name, ignoreCase = true) }
        app.saveServices()
        // Also remove appointments that referenced this service
        app.appointments.removeAll { it.service.name.equals(service.name, ignoreCase = true) }
        app.saveAppointments()
    }

    // ── Clients ──────────────────────────────────────────────────────────────

    fun getClients(): List<User> =
        app.registeredUsers.filter { it.role == UserRole.USER }

    // ── Logged-in admin ──────────────────────────────────────────────────────

    fun getAdminUser() = app.loggedInUser

    fun logout() { app.loggedInUser = null }
}
