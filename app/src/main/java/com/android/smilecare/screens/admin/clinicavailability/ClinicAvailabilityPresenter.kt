package com.android.smilecare.screens.admin.clinicavailability

class ClinicAvailabilityPresenter(
    private val view: ClinicAvailabilityContract.View,
    private val model: ClinicAvailabilityModel
) : ClinicAvailabilityContract.Presenter {

    private var pendingOpenDaysMon0: BooleanArray = BooleanArray(7)
    private var pendingMorningStartMinutes: Int = 8 * 60
    private var pendingMorningEndMinutes: Int = 12 * 60
    private var pendingAfternoonStartMinutes: Int = 13 * 60
    private var pendingAfternoonEndMinutes: Int = 17 * 60
    private var pendingClosedDates: MutableList<ClinicAvailabilityContract.ClosedDate> = mutableListOf()
    private var hasUnsavedChanges: Boolean = false

    override fun load() {
        val schedule = model.loadSchedule()
        pendingOpenDaysMon0 = schedule.openDaysMon0.copyOf()
        pendingMorningStartMinutes = schedule.morningStartMinutes
        pendingMorningEndMinutes = schedule.morningEndMinutes
        pendingAfternoonStartMinutes = schedule.afternoonStartMinutes
        pendingAfternoonEndMinutes = schedule.afternoonEndMinutes
        pendingClosedDates = schedule.closedDates
            .map { ClinicAvailabilityContract.ClosedDate(it.dateYmd, it.reason) }
            .sortedBy { it.dateYmd }
            .toMutableList()
        hasUnsavedChanges = false
        render()
    }

    override fun onDayChanged(dayIndexMon0: Int, isOpen: Boolean) {
        if (dayIndexMon0 !in 0..6) return
        pendingOpenDaysMon0[dayIndexMon0] = isOpen
        hasUnsavedChanges = true
        render()
    }

    override fun onMorningStartPicked(totalMinutes: Int) {
        if (totalMinutes !in 0..(24 * 60)) return
        if (totalMinutes >= pendingMorningEndMinutes) {
            view.showMessage("Morning start must be before morning end")
            return
        }
        pendingMorningStartMinutes = totalMinutes
        hasUnsavedChanges = true
        render()
    }

    override fun onMorningEndPicked(totalMinutes: Int) {
        if (totalMinutes !in 0..(24 * 60)) return
        if (totalMinutes <= pendingMorningStartMinutes) {
            view.showMessage("Morning end must be after morning start")
            return
        }
        if (totalMinutes > pendingAfternoonStartMinutes) {
            view.showMessage("Morning end must be before afternoon start")
            return
        }
        pendingMorningEndMinutes = totalMinutes
        hasUnsavedChanges = true
        render()
    }

    override fun onAfternoonStartPicked(totalMinutes: Int) {
        if (totalMinutes !in 0..(24 * 60)) return
        if (totalMinutes < pendingMorningEndMinutes) {
            view.showMessage("Afternoon start must be after morning end")
            return
        }
        if (totalMinutes >= pendingAfternoonEndMinutes) {
            view.showMessage("Afternoon start must be before afternoon end")
            return
        }
        pendingAfternoonStartMinutes = totalMinutes
        hasUnsavedChanges = true
        render()
    }

    override fun onAfternoonEndPicked(totalMinutes: Int) {
        if (totalMinutes !in 0..(24 * 60)) return
        if (totalMinutes <= pendingAfternoonStartMinutes) {
            view.showMessage("Afternoon end must be after afternoon start")
            return
        }
        pendingAfternoonEndMinutes = totalMinutes
        hasUnsavedChanges = true
        render()
    }

    override fun onAddClosedDate(dateYmd: Int, reason: String) {
        if (dateYmd !in 19000101..21001231) return
        if (pendingClosedDates.any { it.dateYmd == dateYmd }) {
            view.showMessage("That date is already marked as closed")
            return
        }
        pendingClosedDates.add(ClinicAvailabilityContract.ClosedDate(dateYmd, reason.trim()))
        pendingClosedDates.sortBy { it.dateYmd }
        hasUnsavedChanges = true
        render()
    }

    override fun onRemoveClosedDate(dateYmd: Int) {
        val sizeBefore = pendingClosedDates.size
        pendingClosedDates.removeAll { it.dateYmd == dateYmd }
        if (pendingClosedDates.size < sizeBefore) {
            hasUnsavedChanges = true
            render()
        }
    }

    override fun onSaveClicked() {
        val valid =
            pendingMorningStartMinutes in 0..(24 * 60) &&
                pendingMorningEndMinutes in 0..(24 * 60) &&
                pendingAfternoonStartMinutes in 0..(24 * 60) &&
                pendingAfternoonEndMinutes in 0..(24 * 60) &&
                pendingMorningStartMinutes < pendingMorningEndMinutes &&
                pendingAfternoonStartMinutes < pendingAfternoonEndMinutes &&
                pendingMorningEndMinutes <= pendingAfternoonStartMinutes
        if (!valid) {
            view.showMessage("Clinic hours are not valid")
            return
        }
        model.saveSchedule(
            ClinicAvailabilityModel.Schedule(
                openDaysMon0 = pendingOpenDaysMon0.copyOf(),
                morningStartMinutes = pendingMorningStartMinutes,
                morningEndMinutes = pendingMorningEndMinutes,
                afternoonStartMinutes = pendingAfternoonStartMinutes,
                afternoonEndMinutes = pendingAfternoonEndMinutes,
                closedDates = pendingClosedDates.map { ClinicAvailabilityModel.ClosedDate(it.dateYmd, it.reason) }
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
                morningStartMinutes = pendingMorningStartMinutes,
                morningEndMinutes = pendingMorningEndMinutes,
                afternoonStartMinutes = pendingAfternoonStartMinutes,
                afternoonEndMinutes = pendingAfternoonEndMinutes,
                closedDates = pendingClosedDates.toList(),
                hasUnsavedChanges = hasUnsavedChanges
            )
        )
    }
}
