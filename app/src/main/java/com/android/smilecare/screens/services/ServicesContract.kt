package com.android.smilecare.screens.services

import com.android.smilecare.data.DentalService

interface ServicesContract {
    interface View {
        fun showServices(services: List<DentalService>)
    }

    interface Presenter {
        fun loadServices()
    }
}
