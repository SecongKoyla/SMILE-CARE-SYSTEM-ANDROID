package com.android.smilecare.screens.bookappointment

import com.android.smilecare.data.DentalService

interface BookAppointmentContract {

    data class TimeSlot(
        val label: String,
        val isBooked: Boolean
    )

    interface View {
        fun showMessage(message: String)
        fun showServices(services: List<DentalService>, preselectedServiceName: String?)
        fun showSelectedDate(formatted: String)
        fun showTimeSlots(timeSlots: List<TimeSlot>)
        fun setTimeSlotsSectionVisible(visible: Boolean)
        fun resetForm()
    }

    interface Presenter {
        fun onViewReady(preselectedServiceName: String?)
        fun onServiceSelected(service: DentalService?)
        fun getOpenDaysMon0ForValidator(): BooleanArray
        fun getClosedDatesYmdForValidator(): IntArray
        fun onDatePicked(date: java.util.Calendar)
        fun onTimeSlotSelected(slotLabel: String)
        fun onConfirmClicked()
    }
}
