package com.android.smilecare.screens.admin

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import com.android.smilecare.R
import com.android.smilecare.app.CustomApp
import com.android.smilecare.data.Appointment
import com.android.smilecare.data.AppointmentStatus
import com.android.smilecare.data.DentalService
import com.android.smilecare.data.User
import com.android.smilecare.screens.login.LoginActivity
import com.android.smilecare.utils.toast
import java.text.SimpleDateFormat
import java.util.*

class AdminActivity : AppCompatActivity(),
    AdminContract.AllAppointmentsView,
    AdminContract.ManageServicesView,
    AdminContract.RegisteredClientsView {

    private lateinit var model: AdminModel
    private lateinit var allApptPresenter: AllAppointmentsPresenter
    private lateinit var manageServicesPresenter: ManageServicesPresenter
    private lateinit var registeredClientsPresenter: RegisteredClientsPresenter

    // Tab buttons
    private lateinit var tabAllAppointments: TextView
    private lateinit var tabManageServices: TextView
    private lateinit var tabClinicAvailability: TextView
    private lateinit var tabRegisteredClients: TextView

    // Content containers
    private lateinit var contentAllAppointments: ScrollView
    private lateinit var contentManageServices: ScrollView
    private lateinit var contentClinicAvailability: ScrollView
    private lateinit var contentRegisteredClients: ScrollView

    private var selectedDateFilter: Date? = null
    private var currentStatusFilter = "All"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        model = AdminModel(application as CustomApp)
        allApptPresenter = AllAppointmentsPresenter(this, model)
        manageServicesPresenter = ManageServicesPresenter(this, model)
        registeredClientsPresenter = RegisteredClientsPresenter(this, model)

        bindViews()
        setupTopBar()
        setupTabs()
        setupAllAppointmentsTab()

        showTab(TAB_ALL_APPOINTMENTS)
    }

    // ── Bind all views ────────────────────────────────────────────────────────

    private fun bindViews() {
        tabAllAppointments      = findViewById(R.id.tabAllAppointments)
        tabManageServices       = findViewById(R.id.tabManageServices)
        tabClinicAvailability   = findViewById(R.id.tabClinicAvailability)
        tabRegisteredClients    = findViewById(R.id.tabRegisteredClients)

        contentAllAppointments    = findViewById(R.id.contentAllAppointments)
        contentManageServices     = findViewById(R.id.contentManageServices)
        contentClinicAvailability = findViewById(R.id.contentClinicAvailability)
        contentRegisteredClients  = findViewById(R.id.contentRegisteredClients)
    }

    // ── Top bar ───────────────────────────────────────────────────────────────

    private fun setupTopBar() {
        val app = application as CustomApp
        val user = app.loggedInUser
        val nameView = findViewById<TextView>(R.id.textAdminName)
        nameView.text = if (user != null) "${user.firstName} ${user.lastName}" else "Admin"

        nameView.setOnClickListener { showProfileMenu(it) }
        findViewById<View>(R.id.imageAdminProfile).setOnClickListener { showProfileMenu(it) }
        findViewById<TextView>(R.id.textAdminLogout).setOnClickListener {
            model.logout()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun showProfileMenu(anchor: View) {
        val popup = PopupMenu(this, anchor)
        popup.menu.add(0, 1, 0, "View Profile")
        popup.menu.add(0, 2, 1, "Logout")
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> toast("Admin profile — ${(application as CustomApp).loggedInUser?.email}")
                2 -> {
                    model.logout()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            }
            true
        }
        popup.show()
    }

    // ── Tab navigation ────────────────────────────────────────────────────────

    private val TAB_ALL_APPOINTMENTS  = 0
    private val TAB_MANAGE_SERVICES   = 1
    private val TAB_CLINIC_AVAIL      = 2
    private val TAB_CLIENTS           = 3

    private fun setupTabs() {
        tabAllAppointments.setOnClickListener    { showTab(TAB_ALL_APPOINTMENTS) }
        tabManageServices.setOnClickListener     { showTab(TAB_MANAGE_SERVICES) }
        tabClinicAvailability.setOnClickListener { showTab(TAB_CLINIC_AVAIL) }
        tabRegisteredClients.setOnClickListener  { showTab(TAB_CLIENTS) }
    }

    private fun showTab(tab: Int) {
        val tabs     = listOf(tabAllAppointments, tabManageServices, tabClinicAvailability, tabRegisteredClients)
        val contents = listOf(contentAllAppointments, contentManageServices, contentClinicAvailability, contentRegisteredClients)

        tabs.forEachIndexed { i, tv ->
            if (i == tab) {
                tv.setBackgroundResource(R.drawable.bg_tab_active)
                tv.setTextColor(getColor(R.color.teal_dark))
            } else {
                tv.setBackgroundResource(R.drawable.bg_tab_inactive)
                tv.setTextColor(getColor(R.color.text_secondary))
            }
        }
        contents.forEachIndexed { i, v -> v.visibility = if (i == tab) View.VISIBLE else View.GONE }

        when (tab) {
            TAB_ALL_APPOINTMENTS -> allApptPresenter.loadAppointments(currentStatusFilter, selectedDateFilter)
            TAB_MANAGE_SERVICES  -> { manageServicesPresenter.loadServices(); setupAddServiceButton() }
            TAB_CLINIC_AVAIL     -> buildClinicAvailabilityView()
            TAB_CLIENTS          -> registeredClientsPresenter.loadClients()
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // TAB 1 — All Appointments
    // ══════════════════════════════════════════════════════════════════════════

    private fun setupAllAppointmentsTab() {
        // Status filter radio group
        val rg = findViewById<RadioGroup>(R.id.rgAdminStatusFilter)
        rg.setOnCheckedChangeListener { group, checkedId ->
            for (i in 0 until group.childCount) {
                val rb = group.getChildAt(i) as RadioButton
                rb.setBackgroundResource(R.drawable.bg_time_slot)
                rb.setTextColor(getColor(R.color.text_primary))
            }
            val sel = group.findViewById<RadioButton>(checkedId)
            sel?.setBackgroundResource(R.drawable.bg_time_slot_selected)
            sel?.setTextColor(getColor(R.color.white))
            currentStatusFilter = sel?.text?.toString() ?: "All"
            allApptPresenter.loadAppointments(currentStatusFilter, selectedDateFilter)
        }
        // Highlight default "All"
        val rbAll = findViewById<RadioButton>(R.id.rbAdminAll)
        rbAll.setBackgroundResource(R.drawable.bg_time_slot_selected)
        rbAll.setTextColor(getColor(R.color.white))

        // Date picker button
        findViewById<ImageButton>(R.id.btnPickDate).setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(this, { _, y, m, d ->
                val picked = Calendar.getInstance().apply { set(y, m, d, 0, 0, 0); set(Calendar.MILLISECOND, 0) }.time
                selectedDateFilter = picked
                val fmt = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                findViewById<TextView>(R.id.textSelectedDateFilter).text = fmt.format(picked)
                allApptPresenter.loadAppointments(currentStatusFilter, picked)
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        // Clear date filter
        findViewById<TextView>(R.id.textClearDate).setOnClickListener {
            selectedDateFilter = null
            findViewById<TextView>(R.id.textSelectedDateFilter).text = "All dates"
            allApptPresenter.loadAppointments(currentStatusFilter, null)
        }
    }

    // ── AllAppointmentsView ───────────────────────────────────────────────────

    override fun showStats(total: Int, approved: Int, pending: Int, completed: Int, cancelled: Int) {
        findViewById<TextView>(R.id.statTotal).text     = total.toString()
        findViewById<TextView>(R.id.statApproved).text  = approved.toString()
        findViewById<TextView>(R.id.statPending).text   = pending.toString()
        findViewById<TextView>(R.id.statCompleted).text = completed.toString()
        findViewById<TextView>(R.id.statCancelled).text = cancelled.toString()
    }

    override fun showAppointments(list: List<Appointment>) {
        val container = findViewById<LinearLayout>(R.id.adminAppointmentsList)
        container.removeAllViews()

        val emptyView = findViewById<TextView>(R.id.textAdminEmpty)
        if (list.isEmpty()) {
            emptyView.visibility = View.VISIBLE
            return
        }
        emptyView.visibility = View.GONE

        val dateFmt = SimpleDateFormat("MMM dd", Locale.getDefault())
        val monthFmt = SimpleDateFormat("MMM", Locale.getDefault())
        val dayFmt   = SimpleDateFormat("dd", Locale.getDefault())

        list.forEach { appt ->
            val item = LayoutInflater.from(this).inflate(R.layout.item_admin_appointment, container, false)

            item.findViewById<TextView>(R.id.textAdminApptDay).text   = dayFmt.format(appt.date)
            item.findViewById<TextView>(R.id.textAdminApptMonth).text = monthFmt.format(appt.date).uppercase()
            item.findViewById<TextView>(R.id.textAdminApptService).text = appt.service.name
            item.findViewById<TextView>(R.id.textAdminApptTime).text    = appt.timeSlot
            item.findViewById<TextView>(R.id.textAdminApptEmail).text   = appt.userEmail

            val statusView = item.findViewById<TextView>(R.id.textAdminApptStatus)
            statusView.text = appt.status.name.lowercase().replaceFirstChar { it.uppercase() }
            statusView.setBackgroundResource(statusBg(appt.status))
            statusView.setTextColor(statusTextColor(appt.status))

            // Action buttons
            val btnApprove  = item.findViewById<TextView>(R.id.btnAdminApprove)
            val btnComplete = item.findViewById<TextView>(R.id.btnAdminComplete)
            val btnPending  = item.findViewById<TextView>(R.id.btnAdminPending)
            val btnCancel   = item.findViewById<TextView>(R.id.btnAdminCancel)
            val btnDelete   = item.findViewById<TextView>(R.id.btnAdminDelete)

            // Highlight the current-status button
            resetActionButtons(listOf(btnApprove, btnComplete, btnPending, btnCancel))
            when (appt.status) {
                AppointmentStatus.APPROVED  -> highlightButton(btnApprove)
                AppointmentStatus.COMPLETED -> highlightButton(btnComplete)
                AppointmentStatus.PENDING   -> highlightButton(btnPending)
                AppointmentStatus.CANCELLED -> highlightButton(btnCancel)
            }

            btnApprove.setOnClickListener  { allApptPresenter.approveAppointment(appt) }
            btnComplete.setOnClickListener { allApptPresenter.completeAppointment(appt) }
            btnPending.setOnClickListener  { allApptPresenter.setPendingAppointment(appt) }
            btnCancel.setOnClickListener   { allApptPresenter.cancelAppointment(appt) }
            btnDelete.setOnClickListener   {
                AlertDialog.Builder(this)
                    .setTitle("Delete Appointment")
                    .setMessage("Are you sure you want to permanently delete this appointment?")
                    .setPositiveButton("Delete") { _, _ -> allApptPresenter.deleteAppointment(appt) }
                    .setNegativeButton("Cancel", null)
                    .show()
            }

            container.addView(item)
        }
    }

    private fun resetActionButtons(buttons: List<TextView>) {
        buttons.forEach {
            it.setBackgroundResource(R.drawable.bg_action_btn_default)
            it.setTextColor(getColor(R.color.text_primary))
        }
    }

    private fun highlightButton(btn: TextView) {
        btn.setBackgroundResource(R.drawable.bg_button_teal)
        btn.setTextColor(getColor(R.color.white))
    }

    private fun statusBg(s: AppointmentStatus) = when (s) {
        AppointmentStatus.APPROVED  -> R.drawable.bg_status_approved
        AppointmentStatus.PENDING   -> R.drawable.bg_status_pending
        AppointmentStatus.COMPLETED -> R.drawable.bg_status_completed
        AppointmentStatus.CANCELLED -> R.drawable.bg_status_cancelled
    }

    private fun statusTextColor(s: AppointmentStatus) = when (s) {
        AppointmentStatus.APPROVED  -> getColor(R.color.status_approved)
        AppointmentStatus.PENDING   -> getColor(R.color.status_pending)
        AppointmentStatus.COMPLETED -> getColor(R.color.status_completed)
        AppointmentStatus.CANCELLED -> getColor(R.color.status_cancelled)
    }

    // ══════════════════════════════════════════════════════════════════════════
    // TAB 2 — Manage Services
    // ══════════════════════════════════════════════════════════════════════════

    private var addServiceDialog: AlertDialog? = null

    override fun showServices(list: List<DentalService>) {
        val container = findViewById<LinearLayout>(R.id.adminServicesList)
        container.removeAllViews()

        list.forEach { svc ->
            val item = LayoutInflater.from(this).inflate(R.layout.item_admin_service, container, false)
            item.findViewById<TextView>(R.id.textAdminSvcEmoji).text       = svc.emoji
            item.findViewById<TextView>(R.id.textAdminSvcName).text        = svc.name
            item.findViewById<TextView>(R.id.textAdminSvcDescription).text = svc.description
            item.findViewById<TextView>(R.id.textAdminSvcPrice).text       = "₱${svc.price}"
            item.findViewById<TextView>(R.id.textAdminSvcDuration).text    =
                if (svc.durationMinutes >= 60) "${svc.durationMinutes / 60} hr" else "${svc.durationMinutes} min"

            item.findViewById<TextView>(R.id.btnAdminDeleteService).setOnClickListener {
                AlertDialog.Builder(this)
                    .setTitle("Delete Service")
                    .setMessage("Delete \"${svc.name}\"? All related appointments will also be removed.")
                    .setPositiveButton("Delete") { _, _ -> manageServicesPresenter.deleteService(svc) }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
            container.addView(item)
        }
    }

    override fun showAddServiceDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_service, null)
        addServiceDialog = AlertDialog.Builder(this)
            .setTitle("Add New Service")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                manageServicesPresenter.addService(
                    name        = dialogView.findViewById<EditText>(R.id.etSvcName).text.toString(),
                    description = dialogView.findViewById<EditText>(R.id.etSvcDescription).text.toString(),
                    price       = dialogView.findViewById<EditText>(R.id.etSvcPrice).text.toString(),
                    durationMin = dialogView.findViewById<EditText>(R.id.etSvcDuration).text.toString(),
                    emoji       = dialogView.findViewById<EditText>(R.id.etSvcEmoji).text.toString()
                )
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun dismissDialog() { addServiceDialog?.dismiss() }

    // ══════════════════════════════════════════════════════════════════════════
    // TAB 3 — Clinic Availability (static UI for now)
    // ══════════════════════════════════════════════════════════════════════════

    private fun buildClinicAvailabilityView() {
        // Content is static XML — nothing dynamic needed yet
    }

    // ══════════════════════════════════════════════════════════════════════════
    // TAB 4 — Registered Clients
    // ══════════════════════════════════════════════════════════════════════════

    override fun showClients(list: List<User>) {
        val container = findViewById<LinearLayout>(R.id.adminClientsList)
        container.removeAllViews()

        val emptyView = findViewById<TextView>(R.id.textClientsEmpty)
        if (list.isEmpty()) { emptyView.visibility = View.VISIBLE; return }
        emptyView.visibility = View.GONE

        val app = application as CustomApp
        list.forEach { client ->
            val item = LayoutInflater.from(this).inflate(R.layout.item_admin_client, container, false)
            val initial = "${client.firstName.firstOrNull() ?: ""}${client.lastName.firstOrNull() ?: ""}"
            item.findViewById<TextView>(R.id.textClientInitials).text    = initial.uppercase()
            item.findViewById<TextView>(R.id.textClientName).text        = "${client.firstName} ${client.lastName}"
            item.findViewById<TextView>(R.id.textClientEmail).text       = client.email
            val apptCount = app.appointments.count { it.userEmail.equals(client.email, ignoreCase = true) }
            item.findViewById<TextView>(R.id.textClientApptCount).text   = "$apptCount appointment${if (apptCount != 1) "s" else ""}"
            container.addView(item)
        }
    }


    private fun setupAddServiceButton() {
        findViewById<Button>(R.id.btnAddService).setOnClickListener {
            manageServicesPresenter  // ensure presenter is ready
            showAddServiceDialog()
        }
    }

    // ── Shared ────────────────────────────────────────────────────────────────

    override fun showMessage(msg: String) = toast(msg)
}
