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
        val trimmedName = name.trim()
        val trimmedDesc = description.trim()
        if (trimmedName.isBlank()) {
            view.showMessage("Service name is required")
            return
        }
        if (trimmedDesc.isBlank()) {
            view.showMessage("Service description is required")
            return
        }

        val priceInt = price.trim().toIntOrNull()
        if (priceInt == null || priceInt <= 0) {
            view.showMessage("Valid price is required")
            return
        }

        val dur = durationMin.trim().toIntOrNull()
        if (dur == null || dur <= 0) {
            view.showMessage("Valid duration (minutes) is required")
            return
        }

        if (model.getServices().any { it.name.equals(trimmedName, ignoreCase = true) }) {
            view.showMessage("Service already exists")
            return
        }

        val service = DentalService(
            trimmedName,
            trimmedDesc,
            priceInt,
            dur,
            emoji.trim().ifBlank { "🦷" }
        )
        model.addService(service)
        view.showMessage("Service added")
        view.dismissDialog()
        loadServices()
    }

    override fun editService(
        oldService: DentalService, newName: String, newDescription: String,
        newPrice: String, newDurationMin: String, newEmoji: String
    ) {
        val trimmedName = newName.trim()
        val trimmedDesc = newDescription.trim()
        if (trimmedName.isBlank()) {
            view.showMessage("Service name is required")
            return
        }
        if (trimmedDesc.isBlank()) {
            view.showMessage("Service description is required")
            return
        }

        val priceInt = newPrice.trim().toIntOrNull()
        if (priceInt == null || priceInt <= 0) {
            view.showMessage("Valid price is required")
            return
        }

        val dur = newDurationMin.trim().toIntOrNull()
        if (dur == null || dur <= 0) {
            view.showMessage("Valid duration (minutes) is required")
            return
        }

        // Only check for duplicates if the name actually changed
        if (!oldService.name.equals(trimmedName, ignoreCase = true) &&
            model.getServices().any { it.name.equals(trimmedName, ignoreCase = true) }) {
            view.showMessage("Service with this name already exists")
            return
        }

        val updatedService = DentalService(
            trimmedName,
            trimmedDesc,
            priceInt,
            dur,
            newEmoji.trim().ifBlank { "🦷" }
        )
        model.updateService(oldService, updatedService)
        view.showMessage("Service updated")
        view.dismissDialog()
        loadServices()
    }

    override fun deleteService(service: DentalService) {
        model.deleteService(service)
        view.showMessage("Service deleted")
        loadServices()
    }
}
