package com.android.smilecare.screens.admin

import com.android.smilecare.data.Appointment
import com.android.smilecare.data.AppointmentStatus
import java.util.Date

class AllAppointmentsPresenter(
    private val view: AdminContract.AllAppointmentsView,
    private val model: AdminModel
) : AdminContract.AllAppointmentsPresenter {

    private var currentFilter = "All"
    private var currentDate: Date? = null

    override fun loadAppointments(statusFilter: String, dateFilter: Date?) {
        currentFilter = statusFilter
        currentDate = dateFilter

        val base = if (dateFilter != null) {
            model.getAppointmentsByDate(dateFilter)
                .let { list ->
                    if (statusFilter.equals("All", ignoreCase = true)) list
                    else list.filter { it.status.name.equals(statusFilter, ignoreCase = true) }
                }
        } else {
            model.getAppointmentsByStatus(statusFilter)
        }

        val all = model.getAllAppointments()
        view.showStats(
            total     = all.size,
            approved  = all.count { it.status == AppointmentStatus.APPROVED },
            pending   = all.count { it.status == AppointmentStatus.PENDING },
            completed = all.count { it.status == AppointmentStatus.COMPLETED },
            cancelled = all.count { it.status == AppointmentStatus.CANCELLED }
        )
        view.showAppointments(base)
    }

    override fun approveAppointment(appointment: Appointment) {
        model.updateStatus(appointment.id, AppointmentStatus.APPROVED)
        view.showMessage("Appointment approved")
        loadAppointments(currentFilter, currentDate)
    }

    override fun completeAppointment(appointment: Appointment) {
        model.updateStatus(appointment.id, AppointmentStatus.COMPLETED)
        view.showMessage("Marked as completed")
        loadAppointments(currentFilter, currentDate)
    }

    override fun setPendingAppointment(appointment: Appointment) {
        model.updateStatus(appointment.id, AppointmentStatus.PENDING)
        view.showMessage("Marked as pending")
        loadAppointments(currentFilter, currentDate)
    }

    override fun cancelAppointment(appointment: Appointment) {
        model.updateStatus(appointment.id, AppointmentStatus.CANCELLED)
        view.showMessage("Appointment cancelled")
        loadAppointments(currentFilter, currentDate)
    }

    override fun deleteAppointment(appointment: Appointment) {
        model.deleteAppointment(appointment.id)
        view.showMessage("Appointment deleted")
        loadAppointments(currentFilter, currentDate)
    }
}
