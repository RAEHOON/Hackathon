// PostMarkDecorator.kt  (빨간 점: 날짜 숫자 오른쪽 상단)
package com.example.a20251215.MypageFragment

import android.graphics.Canvas
import android.graphics.Paint
import android.text.style.LineBackgroundSpan
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade

class PostMarkDecorator(
    private val days: Set<CalendarDay>,
    private val dotRadiusPx: Float,
    private val color: Int,
    private val offsetXPx: Float,
    private val offsetYPx: Float
) : DayViewDecorator {

    override fun shouldDecorate(day: CalendarDay): Boolean = days.contains(day)

    override fun decorate(view: DayViewFacade) {
        view.addSpan(TopRightDotSpan(dotRadiusPx, color, offsetXPx, offsetYPx))
    }

    private class TopRightDotSpan(
        private val radius: Float,
        private val color: Int,
        private val offsetX: Float,
        private val offsetY: Float
    ) : LineBackgroundSpan {

        private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            this.color = this@TopRightDotSpan.color
        }

        override fun drawBackground(
            c: Canvas,
            p: Paint,
            left: Int,
            right: Int,
            top: Int,
            baseline: Int,
            bottom: Int,
            text: CharSequence,
            start: Int,
            end: Int,
            lnum: Int
        ) {
            // 대략 "숫자 오른쪽 상단" 위치
            val x = right - offsetX
            val y = top + offsetY + radius
            c.drawCircle(x, y, radius, dotPaint)
        }
    }
}
