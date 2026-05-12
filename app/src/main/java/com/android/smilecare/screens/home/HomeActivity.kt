package com.android.smilecare.screens.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.app.AlertDialog
import com.android.smilecare.R
import com.android.smilecare.app.CustomApp
import com.android.smilecare.screens.appointments.AppointmentsFragment
import com.android.smilecare.screens.bookappointment.BookAppointmentFragment
import com.android.smilecare.screens.login.LoginActivity
import com.android.smilecare.screens.profile.ProfileActivity
import com.android.smilecare.screens.services.ServicesFragment
import com.android.smilecare.utils.disableEdgeToEdge
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeActivity : AppCompatActivity(), HomeContract.View {

    private lateinit var presenter: HomePresenter
    private lateinit var homeScrollView: ScrollView
    private lateinit var navHome: LinearLayout
    private lateinit var navAppointments: LinearLayout
    private lateinit var navBook: LinearLayout
    private lateinit var navServices: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        disableEdgeToEdge()
        setContentView(R.layout.activity_home)

        presenter = HomePresenter(this, HomeModel(application as CustomApp))

        homeScrollView = findViewById(R.id.homeScrollView)
        navHome = findViewById(R.id.navHome)
        navAppointments = findViewById(R.id.navAppointments)
        navBook = findViewById(R.id.navBook)
        navServices = findViewById(R.id.navServices)

        presenter.loadHome()

        findViewById<View>(R.id.imageProfile).setOnClickListener { anchor ->
            showProfileMenu(anchor)
        }

        findViewById<View>(R.id.buttonHeroBookAppointment).setOnClickListener {
            navigateToBookAppointment()
        }

        navHome.setOnClickListener { showHomeDashboard(); setActiveNav(0) }
        navAppointments.setOnClickListener { loadFragment(AppointmentsFragment()); setActiveNav(1) }
        navBook.setOnClickListener { navigateToBookAppointment() }
        navServices.setOnClickListener { loadFragment(ServicesFragment()); setActiveNav(3) }

        findViewById<LinearLayout>(R.id.quickActionBook).setOnClickListener {
            navigateToBookAppointment()
        }
        findViewById<LinearLayout>(R.id.quickActionAppointments).setOnClickListener {
            loadFragment(AppointmentsFragment()); setActiveNav(1)
        }
        findViewById<LinearLayout>(R.id.quickActionServices).setOnClickListener {
            loadFragment(ServicesFragment()); setActiveNav(3)
        }

        findViewById<LinearLayout>(R.id.quickActionSystemUpdates).setOnClickListener {
            showSystemUpdatesDialog()
        }

        findViewById<TextView>(R.id.textLogout).setOnClickListener { presenter.logout() }
    }

    override fun onResume() {
        super.onResume()
        updateProfileAvatar()
        updateSystemUpdatesBadge()
    }

    private fun updateSystemUpdatesBadge() {
        val badge = findViewById<TextView>(R.id.textSystemUpdatesBadge)
        val app = application as CustomApp
        val count = app.getSystemUpdatesForLoggedInUser().size
        if (count > 0) {
            badge.text = count.toString()
            badge.visibility = View.VISIBLE
        } else {
            badge.visibility = View.GONE
        }
    }

    private fun showSystemUpdatesDialog() {
        val app = application as CustomApp
        val updates = app.getSystemUpdatesForLoggedInUser()

        val dialogView = layoutInflater.inflate(R.layout.dialog_system_updates, null)
        val countView = dialogView.findViewById<TextView>(R.id.textSystemUpdatesCount)
        val emptyView = dialogView.findViewById<TextView>(R.id.textSystemUpdatesEmpty)
        val listContainer = dialogView.findViewById<LinearLayout>(R.id.layoutSystemUpdatesList)
        val btnClose = dialogView.findViewById<android.widget.Button>(R.id.btnSystemUpdatesClose)
        val btnClear = dialogView.findViewById<android.widget.Button>(R.id.btnSystemUpdatesClear)

        countView.text = updates.size.toString()
        listContainer.removeAllViews()

        if (updates.isEmpty()) {
            emptyView.visibility = View.VISIBLE
            btnClear.alpha = 0.6f
            btnClear.isEnabled = false
        } else {
            emptyView.visibility = View.GONE
            btnClear.alpha = 1f
            btnClear.isEnabled = true
            val fmt = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
            updates.forEach { u ->
                val row = layoutInflater.inflate(R.layout.item_system_update, listContainer, false)
                val timeText = if (u.timeMillis > 0L) fmt.format(Date(u.timeMillis)) else ""
                row.findViewById<TextView>(R.id.textUpdateTime).text = timeText
                row.findViewById<TextView>(R.id.textUpdateMessage).text = u.message
                listContainer.addView(row)
            }
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        btnClose.setOnClickListener { dialog.dismiss() }
        btnClear.setOnClickListener {
            app.clearSystemUpdatesForLoggedInUser()
            updateSystemUpdatesBadge()
            dialog.dismiss()
        }

        dialog.setOnDismissListener { updateSystemUpdatesBadge() }
        dialog.show()
    }

    private fun updateProfileAvatar() {
        val app = application as CustomApp
        val image = findViewById<ImageView>(R.id.imageProfile)
        val photoUri = app.loggedInUser?.photoUri.orEmpty()
        if (photoUri.isNotBlank()) {
            try {
                image.setImageURI(Uri.parse(photoUri))
            } catch (_: Exception) {
                // If persisted permission was lost / URI is invalid, avoid crashing.
                image.setImageResource(R.drawable.profile)
                clearLoggedInUserPhotoUri(app)
            }
        } else {
            image.setImageResource(R.drawable.profile)
        }
    }

    private fun clearLoggedInUserPhotoUri(app: CustomApp) {
        val email = app.loggedInUser?.email ?: return
        val index = app.registeredUsers.indexOfFirst { it.email.equals(email, ignoreCase = true) }
        if (index != -1) {
            app.registeredUsers[index].photoUri = ""
        }
        app.loggedInUser?.photoUri = ""
        app.saveUsers()
    }

    private fun showProfileMenu(anchor: View) {
        val popup = PopupMenu(this, anchor)
        popup.menu.add(0, 1, 0, "View Profile")
        popup.menu.add(0, 2, 1, "Change Password")
        popup.menu.add(0, 3, 2, "Upload Photo")
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> openProfile(ProfileActivity.SECTION_VIEW_PROFILE)
                2 -> openProfile(ProfileActivity.SECTION_CHANGE_PASSWORD)
                3 -> openProfile(ProfileActivity.SECTION_PROFILE_PHOTO)
            }
            true
        }
        popup.show()
    }

    private fun openProfile(section: String) {
        startActivity(
            Intent(this, ProfileActivity::class.java).putExtra(ProfileActivity.EXTRA_OPEN_SECTION, section)
        )
    }

    private fun loadFragment(fragment: Fragment) {
        if (isFinishing || isDestroyed) return
        homeScrollView.visibility = View.GONE
        val tx = supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)

        // Avoid crashing with: "Can not perform this action after onSaveInstanceState".
        if (supportFragmentManager.isStateSaved) {
            tx.commitAllowingStateLoss()
        } else {
            tx.commit()
        }
        findViewById<View>(R.id.fragmentContainer).visibility = View.VISIBLE
    }

    fun navigateToBookAppointment(preselectedServiceName: String? = null) {
        loadFragment(BookAppointmentFragment.newInstance(preselectedServiceName))
        setActiveNav(2)
    }

    private fun showHomeDashboard() {
        findViewById<View>(R.id.fragmentContainer).visibility = View.GONE
        presenter.loadHome()
        homeScrollView.visibility = View.VISIBLE
        updateSystemUpdatesBadge()
    }

    override fun showGreeting(name: String, greeting: String) {
        findViewById<TextView>(R.id.textGreeting).text = "$greeting, $name!"

        val app = application as CustomApp
        val userEmail = app.loggedInUser?.email
        val now = java.util.Date()
        val calNow = java.util.Calendar.getInstance()
        val currentMonth = calNow.get(java.util.Calendar.MONTH)
        val currentYear = calNow.get(java.util.Calendar.YEAR)

        val count = if (userEmail.isNullOrBlank()) {
            0
        } else {
            app.appointments.count { appt ->
                if (!appt.userEmail.equals(userEmail, ignoreCase = true)) return@count false
                if (appt.status == com.android.smilecare.data.AppointmentStatus.CANCELLED) return@count false
                if (!appt.date.after(now)) return@count false

                val cal = java.util.Calendar.getInstance()
                cal.time = appt.date
                cal.get(java.util.Calendar.MONTH) == currentMonth &&
                    cal.get(java.util.Calendar.YEAR) == currentYear
            }
        }
        findViewById<TextView>(R.id.textAppointmentCount).text =
            "You have $count upcoming appointment${if (count != 1) "s" else ""} this month."
    }

    override fun showNextAppointment(date: String, service: String, time: String) {
        findViewById<View>(R.id.cardNextAppointment).visibility = View.VISIBLE
        findViewById<TextView>(R.id.textNextDate).text = date
        findViewById<TextView>(R.id.textNextService).text = service
        findViewById<TextView>(R.id.textNextTime).text = time
    }

    override fun hideNextAppointment() {
        findViewById<View>(R.id.cardNextAppointment).visibility = View.GONE
    }

    override fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun setActiveNav(index: Int) {
        listOf(navHome, navAppointments, navBook, navServices).forEachIndexed { i, nav ->
            if (nav.childCount > 1) {
                (nav.getChildAt(1) as? TextView)?.setTextColor(
                    if (i == index) getColor(R.color.teal_primary) else getColor(R.color.text_secondary)
                )
            }
        }
    }
}
