package com.android.smilecare.screens.admin.clinicavailability

interface ClinicAvailabilityContract {

    data class ClosedDate(
        val dateYmd: Int, // YYYYMMDD
        val reason: String
    )

    data class State(
        val openDaysMon0: BooleanArray,
        val morningStartMinutes: Int,
        val morningEndMinutes: Int,
        val afternoonStartMinutes: Int,
        val afternoonEndMinutes: Int,
        val closedDates: List<ClosedDate>,
        val hasUnsavedChanges: Boolean
    )

    interface View {
        fun render(state: State)
        fun showMessage(message: String)
    }

    interface Presenter {
        fun load()
        fun onDayChanged(dayIndexMon0: Int, isOpen: Boolean)
        fun onMorningStartPicked(totalMinutes: Int)
        fun onMorningEndPicked(totalMinutes: Int)
        fun onAfternoonStartPicked(totalMinutes: Int)
        fun onAfternoonEndPicked(totalMinutes: Int)
        fun onAddClosedDate(dateYmd: Int, reason: String)
        fun onSaveClicked()
    }
}
