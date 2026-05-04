package com.android.smilecare.screens.admin

class RegisteredClientsPresenter(
    private val view: AdminContract.RegisteredClientsView,
    private val model: AdminModel
) : AdminContract.RegisteredClientsPresenter {

    override fun loadClients() {
        val clients = model.getClients()
        if (clients.isEmpty()) view.showMessage("No registered clients yet.")
        view.showClients(clients)
    }
}
