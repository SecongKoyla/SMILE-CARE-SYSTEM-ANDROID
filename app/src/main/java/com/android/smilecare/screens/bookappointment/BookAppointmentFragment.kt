package com.android.smilecare.screens.bookappointment

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.android.smilecare.R
import com.android.smilecare.app.CustomApp
import com.android.smilecare.data.Appointment
import com.android.smilecare.data.AppointmentStatus
import com.android.smilecare.data.DentalService
import com.android.smilecare.utils.toast
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.CompositeDateValidator
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.*

class BookAppointmentFragment : Fragment() {

    private var selectedService: DentalService? = null
    private var selectedDate: Calendar? = null
    private var selectedTime: String? = null

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

    private fun isClinicOpenOn(date: Calendar, app: CustomApp): Boolean {
        val idx = dayIndexMon0(date.get(Calendar.DAY_OF_WEEK))
        if (idx !in 0..6) return false
        return app.clinicOpenDays.getOrElse(idx) { false }
    }

    private fun dayName(date: Calendar): String {
        return SimpleDateFormat("EEEE", Locale.getDefault()).format(date.time)
    }

    private class ClinicOpenDaysValidator(
        private val openDaysMon0: BooleanArray
    ) : CalendarConstraints.DateValidator {

        override fun isValid(date: Long): Boolean {
            // MaterialDatePicker provides UTC-based midnight timestamps.
            val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            utcCal.timeInMillis = date
            val idx = when (utcCal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> 0
                Calendar.TUESDAY -> 1
                Calendar.WEDNESDAY -> 2
                Calendar.THURSDAY -> 3
                Calendar.FRIDAY -> 4
                Calendar.SATURDAY -> 5
                Calendar.SUNDAY -> 6
                else -> -1
            }
            return idx in 0..6 && openDaysMon0.getOrElse(idx) { false }
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            dest.writeBooleanArray(openDaysMon0)
        }

        override fun describeContents(): Int = 0

        companion object {
            @JvmField
            val CREATOR: Parcelable.Creator<ClinicOpenDaysValidator> = object : Parcelable.Creator<ClinicOpenDaysValidator> {
                override fun createFromParcel(source: Parcel): ClinicOpenDaysValidator {
                    return ClinicOpenDaysValidator(source.createBooleanArray() ?: BooleanArray(7))
                }

                override fun newArray(size: Int): Array<ClinicOpenDaysValidator?> = arrayOfNulls(size)
            }
        }
    }

    private fun getTimeSlots(app: CustomApp): List<String> {
        val open = app.clinicOpeningMinutes
        val close = app.clinicClosingMinutes
        if (open < 0 || close < 0 || open >= close) return emptyList()

        val fmt = SimpleDateFormat("h:mm a", Locale.getDefault())
        val cal = Calendar.getInstance()
        val slots = mutableListOf<String>()
        var mins = open
        val stepMinutes = 60
        if (stepMinutes <= 0) return emptyList()
        // Use < close so you can't book *at* the closing time.
        while (mins < close) {
            cal.set(Calendar.HOUR_OF_DAY, mins / 60)
            cal.set(Calendar.MINUTE, mins % 60)
            slots.add(fmt.format(cal.time))
            mins += stepMinutes
        }
        return slots
    }

    companion object {
        private const val ARG_PRESELECT_SERVICE_NAME = "preselect_service_name"

        fun newInstance(preselectedServiceName: String?): BookAppointmentFragment {
            return BookAppointmentFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PRESELECT_SERVICE_NAME, preselectedServiceName)
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_book_appointment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val app = requireActivity().application as CustomApp
        val preselectedServiceName = arguments?.getString(ARG_PRESELECT_SERVICE_NAME)

        // Service selection
        val serviceGroup = view.findViewById<RadioGroup>(R.id.radioGroupServices)
        app.services.forEach { service ->
            val rb = RadioButton(requireContext()).apply {
                text = "${service.emoji} ${service.name}  ·  ₱${service.price}"
                tag = service
                setTextColor(resources.getColor(R.color.text_primary, null))
                textSize = 14f
                setPadding(24, 20, 24, 20)
            }
            serviceGroup.addView(rb)
        }

        serviceGroup.setOnCheckedChangeListener { group, checkedId ->
            val rb = group.findViewById<RadioButton>(checkedId)
            selectedService = rb?.tag as? DentalService
        }

        if (!preselectedServiceName.isNullOrBlank()) {
            for (i in 0 until serviceGroup.childCount) {
                val rb = serviceGroup.getChildAt(i) as? RadioButton ?: continue
                val service = rb.tag as? DentalService ?: continue
                if (service.name.equals(preselectedServiceName, ignoreCase = true)) {
                    rb.isChecked = true
                    break
                }
            }
        }

        // Date picker
        val textSelectedDate = view.findViewById<TextView>(R.id.textSelectedDate)
        view.findViewById<Button>(R.id.buttonPickDate).setOnClickListener {
            if (app.clinicOpenDays.none { it }) {
                toast("Clinic is closed all week")
                return@setOnClickListener
            }

            val constraints = CalendarConstraints.Builder()
                .setValidator(
                    CompositeDateValidator.allOf(
                        listOf(
                            DateValidatorPointForward.now(),
                            ClinicOpenDaysValidator(app.clinicOpenDays.copyOf())
                        )
                    )
                )
                .build()

            val picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select date")
                .setCalendarConstraints(constraints)
                .build()

            picker.addOnPositiveButtonClickListener { selectionUtcMillis: Long? ->
                val sel = selectionUtcMillis ?: return@addOnPositiveButtonClickListener
                val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = sel }

                // Convert from UTC day to local day to avoid off-by-one in some timezones.
                val picked = Calendar.getInstance().apply {
                    set(utcCal.get(Calendar.YEAR), utcCal.get(Calendar.MONTH), utcCal.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                // Extra safety: should already be blocked visually by the validator.
                if (!isClinicOpenOn(picked, app)) {
                    toast("Clinic is closed on ${dayName(picked)}")
                    return@addOnPositiveButtonClickListener
                }

                selectedDate = picked
                selectedTime = null
                val fmt = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
                textSelectedDate.text = fmt.format(selectedDate!!.time)
                loadTimeSlots(view)
            }

            picker.show(parentFragmentManager, "book_date_picker")
        }

        // Confirm
        view.findViewById<Button>(R.id.buttonConfirm).setOnClickListener {
            when {
                selectedService == null -> toast("Please select a service.")
                selectedDate == null -> toast("Please select a date.")
                selectedTime == null -> toast("Please select a time slot.")
                else -> {
                    if (!isClinicOpenOn(selectedDate!!, app)) {
                        toast("Clinic is closed on ${dayName(selectedDate!!)}")
                        return@setOnClickListener
                    }
                    val appt = Appointment(
                        userEmail = app.loggedInUser?.email.orEmpty(),
                        service = selectedService!!,
                        date = selectedDate!!.time,
                        timeSlot = selectedTime!!,
                        status = AppointmentStatus.PENDING
                    )
                    app.appointments.add(appt)
                    app.saveAppointments()
                    toast("Appointment booked successfully!")
                    resetForm(view)
                }
            }
        }
    }

    private fun loadTimeSlots(view: View) {
        val container = view.findViewById<ViewGroup>(R.id.timeSlotsContainer)
        container.removeAllViews()
        val app = requireActivity().application as CustomApp

        if (selectedDate == null) {
            view.findViewById<View>(R.id.timeSlotsSection).visibility = View.GONE
            return
        }

        val timeSlots = getTimeSlots(app)
        if (timeSlots.isEmpty()) {
            toast("Clinic hours are not set correctly")
            view.findViewById<View>(R.id.timeSlotsSection).visibility = View.GONE
            return
        }

        val bookedTimes = app.appointments
            .filter {
                val cal = Calendar.getInstance().apply { time = it.date }
                val sel = selectedDate!!
                cal.get(Calendar.YEAR) == sel.get(Calendar.YEAR) &&
                cal.get(Calendar.DAY_OF_YEAR) == sel.get(Calendar.DAY_OF_YEAR)
                        && it.status != AppointmentStatus.CANCELLED
            }
            .map { it.timeSlot }

        timeSlots.forEach { slot ->
            val isBooked = bookedTimes.contains(slot)
            val btn = Button(requireContext()).apply {
                text = slot
                isEnabled = !isBooked
                setBackgroundResource(if (isBooked) R.drawable.bg_time_slot_booked else R.drawable.bg_time_slot)
                setTextColor(resources.getColor(if (isBooked) R.color.text_secondary else R.color.text_primary, null))
                val params = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    setMargins(8, 8, 8, 8)
                }
                layoutParams = params
            }
            if (!isBooked) {
                btn.setOnClickListener {
                    selectedTime = slot
                    highlightSelected(container, btn)
                }
            }
            container.addView(btn)
        }
        view.findViewById<View>(R.id.timeSlotsSection).visibility = View.VISIBLE
    }

    private fun highlightSelected(container: ViewGroup, selected: Button) {
        for (i in 0 until container.childCount) {
            val btn = container.getChildAt(i) as? Button ?: continue
            if (btn.isEnabled) {
                btn.setBackgroundResource(R.drawable.bg_time_slot)
                btn.setTextColor(resources.getColor(R.color.text_primary, null))
            }
        }
        selected.setBackgroundResource(R.drawable.bg_time_slot_selected)
        selected.setTextColor(resources.getColor(R.color.white, null))
    }

    private fun resetForm(view: View) {
        selectedService = null
        selectedDate = null
        selectedTime = null
        view.findViewById<RadioGroup>(R.id.radioGroupServices).clearCheck()
        view.findViewById<TextView>(R.id.textSelectedDate).text = "No date selected"
        view.findViewById<ViewGroup>(R.id.timeSlotsContainer).removeAllViews()
        view.findViewById<View>(R.id.timeSlotsSection).visibility = View.GONE
    }
}
