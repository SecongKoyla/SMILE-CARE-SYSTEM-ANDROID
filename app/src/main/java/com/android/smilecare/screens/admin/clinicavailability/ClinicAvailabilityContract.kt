package com.android.smilecare.screens.admin.clinicavailability

interface ClinicAvailabilityContract {

    data class State(
        val openDaysMon0: BooleanArray,
        val openingMinutes: Int,
        val closingMinutes: Int,
        val hasUnsavedChanges: Boolean
    )

    interface View {
        fun render(state: State)
        fun showMessage(message: String)
    }

    interface Presenter {
        fun load()
        fun onDayChanged(dayIndexMon0: Int, isOpen: Boolean)
        fun onOpeningMinutesPicked(totalMinutes: Int)
        fun onClosingMinutesPicked(totalMinutes: Int)
        fun onSaveClicked()
    }
}
