package com.android.smilecare.screens.bookappointment

import android.app.DatePickerDialog
import android.os.Bundle
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
import java.text.SimpleDateFormat
import java.util.*

class BookAppointmentFragment : Fragment() {

    private var selectedService: DentalService? = null
    private var selectedDate: Calendar? = null
    private var selectedTime: String? = null
    private val timeSlots = listOf("8:00 AM", "9:00 AM", "10:00 AM", "11:00 AM", "1:00 PM", "2:00 PM", "3:00 PM", "4:00 PM", "5:00 PM")

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
            val cal = Calendar.getInstance()
            val dialog = DatePickerDialog(requireContext(), { _, y, m, d ->
                selectedDate = Calendar.getInstance().apply { set(y, m, d) }
                val fmt = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
                textSelectedDate.text = fmt.format(selectedDate!!.time)
                loadTimeSlots(view)
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
            dialog.datePicker.minDate = System.currentTimeMillis() - 1000
            dialog.show()
        }

        // Confirm
        view.findViewById<Button>(R.id.buttonConfirm).setOnClickListener {
            when {
                selectedService == null -> toast("Please select a service.")
                selectedDate == null -> toast("Please select a date.")
                selectedTime == null -> toast("Please select a time slot.")
                else -> {
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
