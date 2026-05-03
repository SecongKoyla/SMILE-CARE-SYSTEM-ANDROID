package com.android.smilecare.data

import java.util.Date

data class Appointment(
    val id: String = java.util.UUID.randomUUID().toString(),
    val userEmail: String = "",
    val service: DentalService,
    val date: Date,
    val timeSlot: String,
    var status: AppointmentStatus = AppointmentStatus.PENDING
)

enum class AppointmentStatus {
    PENDING, APPROVED, COMPLETED, CANCELLED
}
