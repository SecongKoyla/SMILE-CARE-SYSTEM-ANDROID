package com.android.smilecare.screens.admin.clinicavailability

class ClinicAvailabilityPresenter(
    private val view: ClinicAvailabilityContract.View,
    private val model: ClinicAvailabilityModel
) : ClinicAvailabilityContract.Presenter {

    private var pendingOpenDaysMon0: BooleanArray = BooleanArray(7)
    private var pendingOpeningMinutes: Int = 8 * 60
    private var pendingClosingMinutes: Int = 17 * 60
    private var hasUnsavedChanges: Boolean = false

    override fun load() {
        val schedule = model.loadSchedule()
        pendingOpenDaysMon0 = schedule.openDaysMon0.copyOf()
        pendingOpeningMinutes = schedule.openingMinutes
        pendingClosingMinutes = schedule.closingMinutes
        hasUnsavedChanges = false
        render()
    }

    override fun onDayChanged(dayIndexMon0: Int, isOpen: Boolean) {
        if (dayIndexMon0 !in 0..6) return
        pendingOpenDaysMon0[dayIndexMon0] = isOpen
        hasUnsavedChanges = true
        render()
    }

    override fun onOpeningMinutesPicked(totalMinutes: Int) {
        if (totalMinutes !in 0..(24 * 60)) return
        if (totalMinutes >= pendingClosingMinutes) {
            view.showMessage("Opening time must be before closing time")
            return
        }
        pendingOpeningMinutes = totalMinutes
        hasUnsavedChanges = true
        render()
    }

    override fun onClosingMinutesPicked(totalMinutes: Int) {
        if (totalMinutes !in 0..(24 * 60)) return
        if (totalMinutes <= pendingOpeningMinutes) {
            view.showMessage("Closing time must be after opening time")
            return
        }
        pendingClosingMinutes = totalMinutes
        hasUnsavedChanges = true
        render()
    }

    override fun onSaveClicked() {
        if (pendingOpeningMinutes !in 0..(24 * 60) || pendingClosingMinutes !in 0..(24 * 60) || pendingOpeningMinutes >= pendingClosingMinutes) {
            view.showMessage("Clinic hours are not valid")
            return
        }
        model.saveSchedule(
            ClinicAvailabilityModel.Schedule(
                openDaysMon0 = pendingOpenDaysMon0.copyOf(),
                openingMinutes = pendingOpeningMinutes,
                closingMinutes = pendingClosingMinutes
            )
        )
        hasUnsavedChanges = false
        view.showMessage("Clinic schedule saved")
        render()
    }

    private fun render() {
        view.render(
            ClinicAvailabilityContract.State(
                openDaysMon0 = pendingOpenDaysMon0.copyOf(),
                openingMinutes = pendingOpeningMinutes,
                closingMinutes = pendingClosingMinutes,
                hasUnsavedChanges = hasUnsavedChanges
            )
        )
    }
}
