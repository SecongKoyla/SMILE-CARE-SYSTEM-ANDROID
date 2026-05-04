package com.android.smilecare.screens.admin

import com.android.smilecare.data.DentalService

class ManageServicesPresenter(
    private val view: AdminContract.ManageServicesView,
    private val model: AdminModel
) : AdminContract.ManageServicesPresenter {

    override fun loadServices() {
        view.showServices(model.getServices())
    }

    override fun addService(
        name: String, description: String,
        price: String, durationMin: String, emoji: String
    ) {
        if (name.isBlank()) { view.showMessage("Service name is required"); return }
        val priceInt = price.toIntOrNull() ?: 0
        val dur = durationMin.toIntOrNull() ?: 30
        val service = DentalService(name.trim(), description.trim(), priceInt, dur, emoji.ifBlank { "🦷" })
        model.addService(service)
        view.showMessage("Service added")
        view.dismissDialog()
        loadServices()
    }

    override fun deleteService(service: DentalService) {
        model.deleteService(service)
        view.showMessage("Service deleted")
        loadServices()
    }
}
