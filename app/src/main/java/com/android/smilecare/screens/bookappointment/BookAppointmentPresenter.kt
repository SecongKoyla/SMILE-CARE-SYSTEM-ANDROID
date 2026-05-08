package com.android.smilecare.screens.bookappointment

import com.android.smilecare.data.Appointment
import com.android.smilecare.data.AppointmentStatus
import com.android.smilecare.data.DentalService
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

class BookAppointmentPresenter(
    private val view: BookAppointmentContract.View,
    private val model: BookAppointmentModel
) : BookAppointmentContract.Presenter {

    private var selectedService: DentalService? = null
    private var selectedDate: Calendar? = null
    private var selectedTime: String? = null

    override fun onViewReady(preselectedServiceName: String?) {
        view.showServices(model.getServices(), preselectedServiceName)
        view.setTimeSlotsSectionVisible(false)
    }

    override fun onServiceSelected(service: DentalService?) {
        selectedService = service
    }

    override fun getOpenDaysMon0ForValidator(): BooleanArray {
        return model.getClinicOpenDaysMon0()
    }

    override fun onDatePicked(date: Calendar) {
        if (!isClinicOpenOn(date)) {
            view.showMessage("Clinic is closed on ${dayName(date)}")
            view.showTimeSlots(emptyList())
            view.setTimeSlotsSectionVisible(false)
            return
        }
        selectedDate = date
        selectedTime = null
        val fmt = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
        view.showSelectedDate(fmt.format(date.time))
        val slots = buildTimeSlotsFor(date)
        view.showTimeSlots(slots)
        view.setTimeSlotsSectionVisible(slots.isNotEmpty())
    }

    override fun onTimeSlotSelected(slotLabel: String) {
        selectedTime = slotLabel
    }

    override fun onConfirmClicked() {
        val service = selectedService
        val date = selectedDate
        val time = selectedTime

        when {
            service == null -> view.showMessage("Please select a service.")
            date == null -> view.showMessage("Please select a date.")
            time.isNullOrBlank() -> view.showMessage("Please select a time slot.")
            !isClinicOpenOn(date) -> view.showMessage("Clinic is closed on ${dayName(date)}")
            else -> {
                val email = model.getLoggedInUserEmail()
                val appt = Appointment(
                    id = UUID.randomUUID().toString(),
                    userEmail = email,
                    service = service,
                    date = date.time,
                    timeSlot = time,
                    status = AppointmentStatus.PENDING
                )
                model.addAppointment(appt)
                view.showMessage("Appointment booked successfully!")
                selectedService = null
                selectedDate = null
                selectedTime = null
                view.resetForm()
            }
        }
    }

    private fun isClinicOpenOn(date: Calendar): Boolean {
        val openDays = model.getClinicOpenDaysMon0()
        val idx = dayIndexMon0(date.get(Calendar.DAY_OF_WEEK))
        if (idx !in 0..6) return false
        return openDays.getOrElse(idx) { false }
    }

    private fun dayIndexMon0(dayOfWeek: Int): Int = when (dayOfWeek) {
        Calendar.MONDAY -> 0
        Calendar.TUESDAY -> 1
        Calendar.WEDNESDAY -> 2
        Calendar.THURSDAY -> 3
        Calendar.FRIDAY -> 4
        Calendar.SATURDAY -> 5
        Calendar.SUNDAY -> 6
        else -> -1
    }

    private fun dayName(date: Calendar): String {
        return SimpleDateFormat("EEEE", Locale.getDefault()).format(date.time)
    }

    private fun buildTimeSlotsFor(date: Calendar): List<BookAppointmentContract.TimeSlot> {
        val opening = model.getClinicOpeningMinutes()
        val closing = model.getClinicClosingMinutes()
        if (opening < 0 || closing < 0 || opening >= closing) {
            view.showMessage("Clinic hours are not set correctly")
            return emptyList()
        }

        val slots = generateTimeSlots(opening, closing)
        val booked = bookedTimesForDate(date)
        return slots.map { label ->
            BookAppointmentContract.TimeSlot(label = label, isBooked = booked.contains(label))
        }
    }

    private fun generateTimeSlots(openMinutes: Int, closeMinutes: Int): List<String> {
        val fmt = SimpleDateFormat("h:mm a", Locale.getDefault())
        val cal = Calendar.getInstance()
        val result = mutableListOf<String>()

        var mins = openMinutes
        val stepMinutes = 60
        if (stepMinutes <= 0) return emptyList()
        while (mins < closeMinutes) {
            cal.set(Calendar.HOUR_OF_DAY, mins / 60)
            cal.set(Calendar.MINUTE, mins % 60)
            result.add(fmt.format(cal.time))
            mins += stepMinutes
        }
        return result
    }

    private fun bookedTimesForDate(selected: Calendar): Set<String> {
        val appts = model.getAppointments()
        val selYear = selected.get(Calendar.YEAR)
        val selDay = selected.get(Calendar.DAY_OF_YEAR)

        return appts.asSequence()
            .filter { it.status != AppointmentStatus.CANCELLED }
            .filter {
                val cal = Calendar.getInstance()
                cal.time = it.date
                cal.get(Calendar.YEAR) == selYear && cal.get(Calendar.DAY_OF_YEAR) == selDay
            }
            .map { it.timeSlot }
            .toSet()
    }
}
