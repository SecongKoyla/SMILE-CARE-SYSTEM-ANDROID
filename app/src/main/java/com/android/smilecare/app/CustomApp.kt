package com.android.smilecare.app

import android.app.Application
import com.android.smilecare.data.Appointment
import com.android.smilecare.data.DentalService
import com.android.smilecare.data.User

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

    override fun onCreate() {
        super.onCreate()
        // Seed a demo user
        registeredUsers.add(User("John", "Doe", "test@gmail.com", "1234"))
    }
}
