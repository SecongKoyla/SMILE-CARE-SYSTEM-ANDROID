package com.android.smilecare.screens.admin.clinicavailability

import com.android.smilecare.app.CustomApp

class ClinicAvailabilityModel(private val app: CustomApp) {

    data class ClosedDate(
        val dateYmd: Int,
        val reason: String
    )

    data class Schedule(
        val openDaysMon0: BooleanArray,
        val morningStartMinutes: Int,
        val morningEndMinutes: Int,
        val afternoonStartMinutes: Int,
        val afternoonEndMinutes: Int,
        val closedDates: List<ClosedDate>
    )

    fun loadSchedule(): Schedule {
        return Schedule(
            openDaysMon0 = app.clinicOpenDays.copyOf(),
            morningStartMinutes = app.clinicMorningStartMinutes,
            morningEndMinutes = app.clinicMorningEndMinutes,
            afternoonStartMinutes = app.clinicAfternoonStartMinutes,
            afternoonEndMinutes = app.clinicAfternoonEndMinutes,
            closedDates = app.clinicClosures.map { ClosedDate(it.dateYmd, it.reason) }
        )
    }

    fun saveSchedule(schedule: Schedule) {
        app.clinicOpenDays = schedule.openDaysMon0.copyOf()

        app.clinicMorningStartMinutes = schedule.morningStartMinutes
        app.clinicMorningEndMinutes = schedule.morningEndMinutes
        app.clinicAfternoonStartMinutes = schedule.afternoonStartMinutes
        app.clinicAfternoonEndMinutes = schedule.afternoonEndMinutes

        // Keep legacy fields in sync.
        app.clinicOpeningMinutes = schedule.morningStartMinutes
        app.clinicClosingMinutes = schedule.afternoonEndMinutes

        app.clinicClosures.clear()
        app.clinicClosures.addAll(schedule.closedDates.map { CustomApp.ClinicClosure(it.dateYmd, it.reason) })
        app.saveClinicSchedule()
    }
}
