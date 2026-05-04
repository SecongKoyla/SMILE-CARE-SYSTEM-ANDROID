package com.android.smilecare.app

import android.app.Application
import com.android.smilecare.data.Appointment
import com.android.smilecare.data.AppointmentStatus
import com.android.smilecare.data.DentalService
import com.android.smilecare.data.User
import com.android.smilecare.data.UserRole
import java.util.Date

class CustomApp : Application() {

    var loggedInUser: User? = null

    val registeredUsers = mutableListOf<User>()

    val services = mutableListOf<DentalService>()

    val appointments = mutableListOf<Appointment>()

    private val prefs by lazy { getSharedPreferences("smilecare_prefs", android.content.Context.MODE_PRIVATE) }

    override fun onCreate() {
        super.onCreate()
        loadUsers()
        loadServices()
        loadAppointments()
    }

    private fun seedDefaultServices() {
        services.clear()
        services.addAll(
            listOf(
                DentalService(
                    "Cleaning",
                    "A professional dental cleaning that removes plaque, tartar, and surface stains that regular brushing can't remove.",
                    600,
                    30,
                    "🦷"
                ),
                DentalService(
                    "Filling",
                    "A treatment used to repair cavities or damaged teeth. The decayed part is carefully removed, and the tooth is filled with a durable material.",
                    300,
                    45,
                    "🔧"
                ),
                DentalService(
                    "Root Canal",
                    "A procedure that treats infection inside the tooth by removing the damaged pulp, cleaning the area, and sealing it.",
                    900,
                    60,
                    "🦷"
                ),
                DentalService(
                    "Whitening",
                    "A safe and effective treatment that removes stains and discoloration caused by food, drinks, or aging.",
                    150,
                    45,
                    "✨"
                ),
                DentalService(
                    "Tooth Extraction",
                    "A procedure to safely remove a damaged, decayed, or problematic tooth. Helps relieve pain and prevent infection.",
                    2500,
                    60,
                    "🦷"
                )
            )
        )
    }

    private fun loadUsers() {
        registeredUsers.clear()
        val usersJson = prefs.getString("users", null) ?: "[]"
        val jsonArray = try {
            org.json.JSONArray(usersJson)
        } catch (_: Exception) {
            prefs.edit().remove("users").apply()
            org.json.JSONArray()
        }

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.optJSONObject(i) ?: continue
            registeredUsers.add(
                User(
                    firstName = obj.optString("firstName", ""),
                    lastName  = obj.optString("lastName", ""),
                    email     = obj.optString("email", ""),
                    password  = obj.optString("password", ""),
                    photoUri  = obj.optString("photoUri", ""),
                    role      = try { UserRole.valueOf(obj.optString("role", "USER")) } catch (_: Exception) { UserRole.USER }
                )
            )
        }

        // Seed demo user
        if (registeredUsers.none { it.email.equals("test@gmail.com", ignoreCase = true) }) {
            registeredUsers.add(User("John", "Doe", "test@gmail.com", "1234", role = UserRole.USER))
        }
        // Seed admin user
        if (registeredUsers.none { it.email.equals("test@smilecare.com", ignoreCase = true) }) {
            registeredUsers.add(User("Test", "User", "test@smilecare.com", "123456", role = UserRole.ADMIN))
        }
        saveUsers()
    }

    fun saveUsers() {
        val jsonArray = org.json.JSONArray()
        for (user in registeredUsers) {
            val obj = org.json.JSONObject()
            obj.put("firstName", user.firstName)
            obj.put("lastName", user.lastName)
            obj.put("email", user.email)
            obj.put("password", user.password)
            obj.put("photoUri", user.photoUri)
            obj.put("role", user.role.name)
            jsonArray.put(obj)
        }
        prefs.edit().putString("users", jsonArray.toString()).apply()
    }

    private fun loadServices() {
        val servicesJson = prefs.getString("services", null)
        if (servicesJson.isNullOrBlank()) {
            seedDefaultServices()
            saveServices()
            return
        }

        val jsonArray = try {
            org.json.JSONArray(servicesJson)
        } catch (_: Exception) {
            prefs.edit().remove("services").apply()
            org.json.JSONArray()
        }

        services.clear()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.optJSONObject(i) ?: continue
            val name = obj.optString("name", "").trim()
            val description = obj.optString("description", "").trim()
            val price = obj.optInt("price", -1)
            val duration = obj.optInt("durationMinutes", -1)
            val emoji = obj.optString("emoji", "🦷").ifBlank { "🦷" }

            if (name.isBlank() || description.isBlank() || price < 0 || duration <= 0) continue
            services.add(DentalService(name, description, price, duration, emoji))
        }

        if (services.isEmpty()) {
            seedDefaultServices()
            saveServices()
        }
    }

    fun saveServices() {
        val jsonArray = org.json.JSONArray()
        for (svc in services) {
            val obj = org.json.JSONObject()
            obj.put("name", svc.name)
            obj.put("description", svc.description)
            obj.put("price", svc.price)
            obj.put("durationMinutes", svc.durationMinutes)
            obj.put("emoji", svc.emoji)
            jsonArray.put(obj)
        }
        prefs.edit().putString("services", jsonArray.toString()).apply()
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
            } catch (_: Exception) { AppointmentStatus.PENDING }

            appointments.add(
                Appointment(
                    id        = obj.optString("id", java.util.UUID.randomUUID().toString()),
                    userEmail = obj.optString("userEmail", ""),
                    service   = service,
                    date      = Date(millis),
                    timeSlot  = obj.optString("timeSlot", ""),
                    status    = status
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
