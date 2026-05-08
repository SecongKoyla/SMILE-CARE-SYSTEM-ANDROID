package com.android.smilecare.screens.services

import com.android.smilecare.app.CustomApp
import com.android.smilecare.data.DentalService

class ServicesModel(private val app: CustomApp) {
    fun getServices(): List<DentalService> = app.services.toList()
}
