package com.android.smilecare.screens.admin

import com.android.smilecare.data.Appointment
import com.android.smilecare.data.DentalService
import com.android.smilecare.data.User

class AdminContract {

    // ── All Appointments tab ─────────────────────────────────────────────────
    interface AllAppointmentsView {
        fun showAppointments(list: List<Appointment>)
        fun showStats(total: Int, approved: Int, pending: Int, completed: Int, cancelled: Int)
        fun showMessage(msg: String)
    }

    interface AllAppointmentsPresenter {
        fun loadAppointments(statusFilter: String = "All", dateFilter: java.util.Date? = null)
        fun approveAppointment(appointment: Appointment)
        fun completeAppointment(appointment: Appointment)
        fun setPendingAppointment(appointment: Appointment)
        fun cancelAppointment(appointment: Appointment)
        fun deleteAppointment(appointment: Appointment)
    }

    // ── Manage Services tab ──────────────────────────────────────────────────
    interface ManageServicesView {
        fun showServices(list: List<DentalService>)
        fun showMessage(msg: String)
        fun showAddServiceDialog()
        fun dismissDialog()
    }

    interface ManageServicesPresenter {
        fun loadServices()
        fun addService(name: String, description: String, price: String, durationMin: String, emoji: String)
        fun deleteService(service: DentalService)
    }

    // ── Registered Clients tab ───────────────────────────────────────────────
    interface RegisteredClientsView {
        fun showClients(list: List<User>)
        fun showMessage(msg: String)
    }

    interface RegisteredClientsPresenter {
        fun loadClients()
    }
}
