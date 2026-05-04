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
import com.android.smilecare.R
import com.android.smilecare.app.CustomApp
import com.android.smilecare.screens.appointments.AppointmentsFragment
import com.android.smilecare.screens.bookappointment.BookAppointmentFragment
import com.android.smilecare.screens.login.LoginActivity
import com.android.smilecare.screens.profile.ProfileActivity
import com.android.smilecare.screens.services.ServicesFragment

class HomeActivity : AppCompatActivity(), HomeContract.View {

    private lateinit var presenter: HomePresenter
    private lateinit var homeScrollView: ScrollView
    private lateinit var navHome: LinearLayout
    private lateinit var navAppointments: LinearLayout
    private lateinit var navBook: LinearLayout
    private lateinit var navServices: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        findViewById<TextView>(R.id.textLogout).setOnClickListener { presenter.logout() }
    }

    override fun onResume() {
        super.onResume()
        updateProfileAvatar()
    }

    private fun updateProfileAvatar() {
        val app = application as CustomApp
        val image = findViewById<ImageView>(R.id.imageProfile)
        val photoUri = app.loggedInUser?.photoUri.orEmpty()
        if (photoUri.isNotBlank()) {
            image.setImageURI(Uri.parse(photoUri))
        } else {
            image.setImageResource(R.drawable.profile)
        }
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
        homeScrollView.visibility = View.GONE
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
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
            (nav.getChildAt(1) as? TextView)?.setTextColor(
                if (i == index) getColor(R.color.teal_primary) else getColor(R.color.text_secondary)
            )
        }
    }
}
