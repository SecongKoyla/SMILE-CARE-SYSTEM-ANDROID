package com.android.smilecare.screens.admin.clinicavailability

import com.android.smilecare.app.CustomApp

class ClinicAvailabilityModel(private val app: CustomApp) {

    data class Schedule(
        val openDaysMon0: BooleanArray,
        val openingMinutes: Int,
        val closingMinutes: Int
    )

    fun loadSchedule(): Schedule {
        return Schedule(
            openDaysMon0 = app.clinicOpenDays.copyOf(),
            openingMinutes = app.clinicOpeningMinutes,
            closingMinutes = app.clinicClosingMinutes
        )
    }

    fun saveSchedule(schedule: Schedule) {
        app.clinicOpenDays = schedule.openDaysMon0.copyOf()
        app.clinicOpeningMinutes = schedule.openingMinutes
        app.clinicClosingMinutes = schedule.closingMinutes
        app.saveClinicSchedule()
    }
}
