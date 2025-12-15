package com.example.a20251215.MypageFragment

import android.graphics.Color
import android.graphics.Typeface
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import org.threeten.bp.DayOfWeek

/** ✅ 일요일: 무조건 빨강 */

class SundayDecorator : DayViewDecorator {
    override fun shouldDecorate(day: CalendarDay): Boolean {
        return day.date.dayOfWeek == DayOfWeek.SUNDAY
    }

    override fun decorate(view: DayViewFacade) {
        view.addSpan(ForegroundColorSpan(Color.RED))
    }
}

/** ✅ 공휴일: 빨강(+굵게는 옵션) */
class HolidayDecorator(
    private val holidays: Set<CalendarDay>
) : DayViewDecorator {

    override fun shouldDecorate(day: CalendarDay): Boolean {
        return holidays.contains(day)
    }

    override fun decorate(view: DayViewFacade) {
        view.addSpan(ForegroundColorSpan(Color.RED))
        view.addSpan(StyleSpan(Typeface.BOLD)) // 원하면 삭제 가능
    }
}
