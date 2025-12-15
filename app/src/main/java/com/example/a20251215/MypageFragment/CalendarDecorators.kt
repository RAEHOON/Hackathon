package com.example.a20251215.holiday

import android.graphics.Color
import android.graphics.Typeface
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import java.util.Calendar

class SundayDecorator(year: Int, month1to12: Int) : DayViewDecorator {
    private val sundays: Set<CalendarDay> = buildSundays(year, month1to12)

    override fun shouldDecorate(day: CalendarDay): Boolean = sundays.contains(day)

    override fun decorate(view: DayViewFacade) {
        view.addSpan(ForegroundColorSpan(Color.RED))
        view.addSpan(StyleSpan(Typeface.BOLD))
    }

    private fun buildSundays(year: Int, month1to12: Int): Set<CalendarDay> {
        val cal = Calendar.getInstance()
        cal.set(year, month1to12 - 1, 1)

        val max = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val set = HashSet<CalendarDay>(max)

        for (d in 1..max) {
            cal.set(year, month1to12 - 1, d)
            if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
//                set.add(CalendarDay.from(cal))
            }
        }
        return set
    }
}

class HolidayDecorator(holidays: Set<CalendarDay>) : DayViewDecorator {
    private val holidaySet = holidays

    override fun shouldDecorate(day: CalendarDay): Boolean = holidaySet.contains(day)

    override fun decorate(view: DayViewFacade) {
        view.addSpan(ForegroundColorSpan(Color.RED))
        view.addSpan(StyleSpan(Typeface.BOLD))
    }
}
