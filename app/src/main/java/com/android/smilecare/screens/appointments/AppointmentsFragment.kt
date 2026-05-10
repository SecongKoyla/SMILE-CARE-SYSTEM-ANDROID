package com.android.smilecare.screens.appointments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.android.smilecare.R
import com.android.smilecare.app.CustomApp
import com.android.smilecare.data.Appointment
import com.android.smilecare.data.AppointmentStatus
import com.android.smilecare.utils.toast
import java.text.SimpleDateFormat
import java.util.Locale

class AppointmentsFragment : Fragment(), AppointmentsContract.View {

    private lateinit var presenter: AppointmentsContract.Presenter
    private lateinit var appointmentsList: LinearLayout
    private lateinit var textEmpty: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_appointments, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        appointmentsList = view.findViewById(R.id.appointmentsList)
        textEmpty = view.findViewById(R.id.textEmpty)

        presenter = AppointmentsPresenter(this, requireActivity().application as CustomApp)

        val rgStatusFilter = view.findViewById<RadioGroup>(R.id.rgStatusFilter)
        
        // Listen to radio group changes to update filters and styling
        rgStatusFilter.setOnCheckedChangeListener { group, checkedId ->
            // Reset background for all
            for (i in 0 until group.childCount) {
                val rb = group.getChildAt(i) as? RadioButton ?: continue
                rb.setBackgroundResource(R.drawable.bg_time_slot)
                rb.setTextColor(resources.getColor(R.color.text_primary, null))
            }

            // Highlight selected
            val selectedRb = group.findViewById<RadioButton>(checkedId)
            if (selectedRb != null) {
                selectedRb.setBackgroundResource(R.drawable.bg_time_slot_selected)
                selectedRb.setTextColor(resources.getColor(R.color.white, null))
                
                val filterText = selectedRb.text.toString()
                presenter.loadAppointments(filterText)
            }
        }

        // Initialize styling for checked radio button and load all items
        view.findViewById<RadioButton>(R.id.rbAll)?.let { initialRb ->
            initialRb.setBackgroundResource(R.drawable.bg_time_slot_selected)
            initialRb.setTextColor(resources.getColor(R.color.white, null))
        }
        
        presenter.loadAppointments("All")
    }

    override fun showAppointments(appointments: List<Appointment>) {
        val ctx = context ?: return
        appointmentsList.removeAllViews()
        val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())

        appointments.forEach { appt ->
            val item = LayoutInflater.from(ctx)
                .inflate(R.layout.item_appointment, appointmentsList, false)

            item.findViewById<TextView>(R.id.textApptDate).text = dateFormat.format(appt.date)
            item.findViewById<TextView>(R.id.textApptService).text = appt.service.name
            item.findViewById<TextView>(R.id.textApptTime).text = appt.timeSlot
            
            val statusView = item.findViewById<TextView>(R.id.textApptStatus)
            statusView.text = appt.status.name
            statusView.setBackgroundResource(getStatusBackground(appt.status))

            val cancelBtn = item.findViewById<TextView>(R.id.btnCancelAppt)
            if (appt.status == AppointmentStatus.PENDING || appt.status == AppointmentStatus.APPROVED) {
                cancelBtn.visibility = View.VISIBLE
                cancelBtn.setOnClickListener {
                    presenter.cancelAppointment(appt)
                }
            } else {
                cancelBtn.visibility = View.GONE
            }

            appointmentsList.addView(item)
        }
    }

    override fun showEmptyState(show: Boolean) {
        textEmpty.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun showMessage(msg: String) {
        toast(msg)
    }

    private fun getStatusBackground(status: AppointmentStatus): Int {
        return when (status) {
            AppointmentStatus.APPROVED -> R.drawable.bg_status_approved
            AppointmentStatus.PENDING -> R.drawable.bg_status_pending
            AppointmentStatus.COMPLETED -> R.drawable.bg_status_completed
            AppointmentStatus.CANCELLED -> R.drawable.bg_status_cancelled
        }
    }
}
