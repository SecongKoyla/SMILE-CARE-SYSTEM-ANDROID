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
import com.android.smilecare.data.DentalService
import com.android.smilecare.utils.toast
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.CompositeDateValidator
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import java.util.*

class BookAppointmentFragment : Fragment(), BookAppointmentContract.View {

    private lateinit var presenter: BookAppointmentContract.Presenter

    private lateinit var serviceGroup: RadioGroup
    private lateinit var textSelectedDate: TextView
    private lateinit var timeSlotsContainer: ViewGroup
    private lateinit var timeSlotsSection: View

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
        val preselectedServiceName = arguments?.getString(ARG_PRESELECT_SERVICE_NAME)

        presenter = BookAppointmentPresenter(
            this,
            BookAppointmentModel(requireActivity().application as CustomApp)
        )

        serviceGroup = view.findViewById(R.id.radioGroupServices)
        textSelectedDate = view.findViewById(R.id.textSelectedDate)
        timeSlotsContainer = view.findViewById(R.id.timeSlotsContainer)
        timeSlotsSection = view.findViewById(R.id.timeSlotsSection)

        presenter.onViewReady(preselectedServiceName)

        // Service selection
        serviceGroup.setOnCheckedChangeListener { group, checkedId ->
            val rb = group.findViewById<RadioButton>(checkedId)
            presenter.onServiceSelected(rb?.tag as? DentalService)
        }

        // Date picker
        view.findViewById<Button>(R.id.buttonPickDate).setOnClickListener {
            if (!isAdded || parentFragmentManager.isStateSaved) return@setOnClickListener
            val openDays = presenter.getOpenDaysMon0ForValidator()
            if (openDays.none { it }) {
                toast("Clinic is closed all week")
                return@setOnClickListener
            }

            val constraints = CalendarConstraints.Builder()
                .setValidator(
                    CompositeDateValidator.allOf(
                        listOf(
                            DateValidatorPointForward.now(),
                            ClinicOpenDaysValidator(openDays)
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

                presenter.onDatePicked(picked)
            }

            picker.show(parentFragmentManager, "book_date_picker")
        }

        // Confirm
        view.findViewById<Button>(R.id.buttonConfirm).setOnClickListener {
            presenter.onConfirmClicked()
        }
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

    // ── MVP View ────────────────────────────────────────────────────────────

    override fun showMessage(message: String) = toast(message)

    override fun showServices(services: List<DentalService>, preselectedServiceName: String?) {
        serviceGroup.removeAllViews()
        services.forEach { service ->
            val rb = RadioButton(requireContext()).apply {
                text = "${service.emoji} ${service.name}  ·  ₱${service.price}"
                tag = service
                setTextColor(resources.getColor(R.color.text_primary, null))
                textSize = 14f
                setPadding(24, 20, 24, 20)
            }
            serviceGroup.addView(rb)
        }

        if (!preselectedServiceName.isNullOrBlank()) {
            for (i in 0 until serviceGroup.childCount) {
                val rb = serviceGroup.getChildAt(i) as? RadioButton ?: continue
                val service = rb.tag as? DentalService ?: continue
                if (service.name.equals(preselectedServiceName, ignoreCase = true)) {
                    rb.isChecked = true
                    presenter.onServiceSelected(service)
                    break
                }
            }
        }
    }

    override fun showSelectedDate(formatted: String) {
        textSelectedDate.text = formatted
    }

    override fun showTimeSlots(timeSlots: List<BookAppointmentContract.TimeSlot>) {
        timeSlotsContainer.removeAllViews()
        timeSlots.forEach { slot ->
            val btn = Button(requireContext()).apply {
                text = slot.label
                isEnabled = !slot.isBooked
                setBackgroundResource(if (slot.isBooked) R.drawable.bg_time_slot_booked else R.drawable.bg_time_slot)
                setTextColor(resources.getColor(if (slot.isBooked) R.color.text_secondary else R.color.text_primary, null))
                val params = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    setMargins(8, 8, 8, 8)
                }
                layoutParams = params
            }
            if (!slot.isBooked) {
                btn.setOnClickListener {
                    presenter.onTimeSlotSelected(slot.label)
                    highlightSelected(timeSlotsContainer, btn)
                }
            }
            timeSlotsContainer.addView(btn)
        }
    }

    override fun setTimeSlotsSectionVisible(visible: Boolean) {
        timeSlotsSection.visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun resetForm() {
        serviceGroup.clearCheck()
        textSelectedDate.text = "No date selected"
        timeSlotsContainer.removeAllViews()
        timeSlotsSection.visibility = View.GONE
    }
}
