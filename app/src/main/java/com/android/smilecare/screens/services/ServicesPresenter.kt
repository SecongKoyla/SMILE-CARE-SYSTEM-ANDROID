package com.android.smilecare.screens.services

class ServicesPresenter(
    private val view: ServicesContract.View,
    private val model: ServicesModel
) : ServicesContract.Presenter {

    override fun loadServices() {
        view.showServices(model.getServices())
    }
}
