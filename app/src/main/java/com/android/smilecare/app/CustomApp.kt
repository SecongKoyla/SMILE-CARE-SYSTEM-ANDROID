package com.android.smilecare.app

import android.app.Application
import android.util.Log
import com.android.smilecare.data.Appointment
import com.android.smilecare.data.AppointmentStatus
import com.android.smilecare.data.DentalService
import com.android.smilecare.data.User
import com.android.smilecare.data.UserRole
import java.io.PrintWriter
import java.io.StringWriter
import java.util.Date
import kotlin.system.exitProcess

class CustomApp : Application() {

    var loggedInUser: User? = null

    val registeredUsers = mutableListOf<User>()

    val services = mutableListOf<DentalService>()

    val appointments = mutableListOf<Appointment>()

    // Clinic availability (Mon..Sun)
    var clinicOpenDays: BooleanArray = BooleanArray(7) { it < 5 } // default: Mon-Fri open
    var clinicOpeningMinutes: Int = 8 * 60
    var clinicClosingMinutes: Int = 17 * 60

    private val prefs by lazy { getSharedPreferences("smilecare_prefs", android.content.Context.MODE_PRIVATE) }

    private val prefLastCrash = "last_crash"
    private val prefLastCrashTime = "last_crash_time"

    override fun onCreate() {
        super.onCreate()

        // If the app died previously, print the root exception on next launch.
        printAndClearLastCrashIfAny()
        installCrashRecorder()

        val initialized = runCatching {
            loadClinicSchedule()
            loadUsers()
            loadServices()
            loadAppointments()
        }.isSuccess

        if (!initialized) {
            // If anything went wrong during cold-start initialization (often due to corrupted
            // preferences), reset and re-seed instead of crashing the whole process.
            prefs.edit().clear().apply()
            loadClinicSchedule()
            loadUsers()
            loadServices()
            loadAppointments()
        }
    }

    private fun printAndClearLastCrashIfAny() {
        val crash = prefs.getString(prefLastCrash, null) ?: return
        val time = prefs.getLong(prefLastCrashTime, 0L)
        Log.e("SmileCareCrash", "Previous crash (timeMillis=$time):\n$crash")
        prefs.edit().remove(prefLastCrash).remove(prefLastCrashTime).apply()
    }

    private fun installCrashRecorder() {
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                // Log immediately so Logcat shows the real crash cause (not only the later
                // InputDispatcher "channel broken" message).
                Log.e("SmileCareCrash", "Uncaught exception on thread=${thread.name}", throwable)

                val sw = StringWriter()
                throwable.printStackTrace(PrintWriter(sw))
                val payload = buildString {
                    append("Thread=").append(thread.name).append('\n')
                    append(throwable::class.java.name)
                    throwable.message?.let { append(": ").append(it) }
                    append('\n')
                    append(sw.toString())
                }
                // Use commit() because the process is about to die.
                prefs.edit()
                    .putLong(prefLastCrashTime, System.currentTimeMillis())
                    .putString(prefLastCrash, payload)
                    .commit()
            } catch (_: Exception) {
                // Best-effort only.
            }

            if (previous != null) {
                previous.uncaughtException(thread, throwable)
            } else {
                android.os.Process.killProcess(android.os.Process.myPid())
                exitProcess(10)
            }
        }
    }

    private fun loadClinicSchedule() {
        val json = prefs.getString("clinic_schedule", null)
        if (json.isNullOrBlank()) {
            saveClinicSchedule()
            return
        }

        val obj = try {
            org.json.JSONObject(json)
        } catch (_: Exception) {
            prefs.edit().remove("clinic_schedule").apply()
            return
        }

        val days = obj.optJSONArray("openDays")
        if (days != null && days.length() == 7) {
            val arr = BooleanArray(7)
            for (i in 0..6) arr[i] = days.optBoolean(i, i < 5)
            clinicOpenDays = arr
        }

        val openM = obj.optInt("openingMinutes", clinicOpeningMinutes)
        val closeM = obj.optInt("closingMinutes", clinicClosingMinutes)
        if (openM in 0..(24 * 60) && closeM in 0..(24 * 60) && openM < closeM) {
            clinicOpeningMinutes = openM
            clinicClosingMinutes = closeM
        }
    }

    fun saveClinicSchedule() {
        val obj = org.json.JSONObject()
        val days = org.json.JSONArray()
        for (i in 0..6) days.put(if (i < clinicOpenDays.size) clinicOpenDays[i] else false)
        obj.put("openDays", days)
        obj.put("openingMinutes", clinicOpeningMinutes)
        obj.put("closingMinutes", clinicClosingMinutes)
        prefs.edit().putString("clinic_schedule", obj.toString()).apply()
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

        var didMutate = false

        // Seed demo user
        if (registeredUsers.none { it.email.equals("test@gmail.com", ignoreCase = true) }) {
            registeredUsers.add(User("John", "Doe", "test@gmail.com", "1234", role = UserRole.USER))
            didMutate = true
        }
        // Seed admin user
        if (registeredUsers.none { it.email.equals("test@smilecare.com", ignoreCase = true) }) {
            registeredUsers.add(User("Test", "User", "test@smilecare.com", "123456", role = UserRole.ADMIN))
            didMutate = true
        }

        if (didMutate) saveUsers()
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
