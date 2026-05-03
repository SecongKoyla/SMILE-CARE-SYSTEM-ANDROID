package com.android.smilecare.screens.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.android.smilecare.R
import com.android.smilecare.app.CustomApp
import com.android.smilecare.screens.appointments.AppointmentsFragment
import com.android.smilecare.screens.bookappointment.BookAppointmentFragment
import com.android.smilecare.screens.login.LoginActivity
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

        navHome.setOnClickListener { showHomeDashboard(); setActiveNav(0) }
        navAppointments.setOnClickListener { loadFragment(AppointmentsFragment()); setActiveNav(1) }
        navBook.setOnClickListener { loadFragment(BookAppointmentFragment()); setActiveNav(2) }
        navServices.setOnClickListener { loadFragment(ServicesFragment()); setActiveNav(3) }

        findViewById<LinearLayout>(R.id.quickActionBook).setOnClickListener {
            loadFragment(BookAppointmentFragment()); setActiveNav(2)
        }
        findViewById<LinearLayout>(R.id.quickActionAppointments).setOnClickListener {
            loadFragment(AppointmentsFragment()); setActiveNav(1)
        }
        findViewById<LinearLayout>(R.id.quickActionServices).setOnClickListener {
            loadFragment(ServicesFragment()); setActiveNav(3)
        }

        findViewById<TextView>(R.id.textLogout).setOnClickListener { presenter.logout() }
    }

    private fun loadFragment(fragment: Fragment) {
        homeScrollView.visibility = View.GONE
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
        findViewById<View>(R.id.fragmentContainer).visibility = View.VISIBLE
    }

    private fun showHomeDashboard() {
        findViewById<View>(R.id.fragmentContainer).visibility = View.GONE
        presenter.loadHome()
        homeScrollView.visibility = View.VISIBLE
    }

    override fun showGreeting(name: String, greeting: String) {
        findViewById<TextView>(R.id.textGreeting).text = "$greeting, $name!"
        val count = (application as CustomApp).appointments.size
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
