package com.android.smilecare.app

import android.app.Application
import com.android.smilecare.data.Appointment
import com.android.smilecare.data.AppointmentStatus
import com.android.smilecare.data.DentalService
import com.android.smilecare.data.User
import java.util.Date

class CustomApp : Application() {

    var loggedInUser: User? = null

    val registeredUsers = mutableListOf<User>()

    val services = mutableListOf(
        DentalService("Cleaning", "A professional dental cleaning that removes plaque, tartar, and surface stains that regular brushing can't remove.", 600, 30, "🦷"),
        DentalService("Filling", "A treatment used to repair cavities or damaged teeth. The decayed part is carefully removed, and the tooth is filled with a durable material.", 300, 45, "🔧"),
        DentalService("Root Canal", "A procedure that treats infection inside the tooth by removing the damaged pulp, cleaning the area, and sealing it.", 900, 60, "🦷"),
        DentalService("Whitening", "A safe and effective treatment that removes stains and discoloration caused by food, drinks, or aging.", 150, 45, "✨"),
        DentalService("Tooth Extraction", "A procedure to safely remove a damaged, decayed, or problematic tooth. Helps relieve pain and prevent infection.", 2500, 60, "🦷")
    )

    val appointments = mutableListOf<Appointment>()

    private val prefs by lazy { getSharedPreferences("smilecare_prefs", android.content.Context.MODE_PRIVATE) }

    override fun onCreate() {
        super.onCreate()
        loadUsers()
        loadAppointments()
    }

    private fun loadUsers() {
        registeredUsers.clear()

        val usersJson = prefs.getString("users", null) ?: "[]"
        val jsonArray = try {
            org.json.JSONArray(usersJson)
        } catch (_: Exception) {
            // If preferences got corrupted (or legacy value was null), recover gracefully.
            prefs.edit().remove("users").apply()
            org.json.JSONArray()
        }

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.optJSONObject(i) ?: continue
            registeredUsers.add(
                User(
                    firstName = obj.optString("firstName", ""),
                    lastName = obj.optString("lastName", ""),
                    email = obj.optString("email", ""),
                    password = obj.optString("password", "")
                )
            )
        }
        if (registeredUsers.isEmpty()) {
            registeredUsers.add(User("John", "Doe", "test@gmail.com", "1234"))
            saveUsers()
        }
    }

    fun saveUsers() {
        val jsonArray = org.json.JSONArray()
        for (user in registeredUsers) {
            val obj = org.json.JSONObject()
            obj.put("firstName", user.firstName)
            obj.put("lastName", user.lastName)
            obj.put("email", user.email)
            obj.put("password", user.password)
            jsonArray.put(obj)
        }
        prefs.edit().putString("users", jsonArray.toString()).apply()
    }

    private fun loadAppointments() {
        val apptsJson = prefs.getString("appointments", null) ?: "[]"
        val jsonArray = try {
            org.json.JSONArray(apptsJson)
        } catch (_: Exception) {
            prefs.edit().remove("appointments").apply()
            org.json.JSONArray()
        }

        appointments.clear()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.optJSONObject(i) ?: continue
            val serviceName = obj.optString("serviceName", "")
            val service = services.firstOrNull { it.name.equals(serviceName, ignoreCase = true) } ?: continue

            val millis = obj.optLong("dateMillis", -1L)
            if (millis <= 0L) continue

            val status = try {
                AppointmentStatus.valueOf(obj.optString("status", AppointmentStatus.PENDING.name))
            } catch (_: Exception) {
                AppointmentStatus.PENDING
            }

            appointments.add(
                Appointment(
                    id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                    userEmail = obj.optString("userEmail", ""),
                    service = service,
                    date = Date(millis),
                    timeSlot = obj.optString("timeSlot", ""),
                    status = status
                )
            )
        }
    }

    fun saveAppointments() {
        val jsonArray = org.json.JSONArray()
        for (appt in appointments) {
            val obj = org.json.JSONObject()
            obj.put("id", appt.id)
            obj.put("userEmail", appt.userEmail)
            obj.put("serviceName", appt.service.name)
            obj.put("dateMillis", appt.date.time)
            obj.put("timeSlot", appt.timeSlot)
            obj.put("status", appt.status.name)
            jsonArray.put(obj)
        }
        prefs.edit().putString("appointments", jsonArray.toString()).apply()
    }
}
