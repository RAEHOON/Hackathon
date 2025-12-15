package com.example.a20251215.MypageFragment

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.a20251215.PublicData.PublicDataClient
import com.example.a20251215.R
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar
import java.util.HashSet
import android.text.style.ForegroundColorSpan

class MypageFragment : Fragment() {

    private var quoteJob: Job? = null

    private lateinit var tvNickname: TextView
    private lateinit var tvProfileSub: TextView
    private lateinit var calendarView: MaterialCalendarView

    // ✅ 공공데이터포털 서비스키(Encoding 키면 encoded된 %2F... 형태)
    private val SERVICE_KEY = "여기에_서비스키_붙여넣기"

    // 공휴일(이번에 표시할 달) 저장
    private val holidaySet = HashSet<CalendarDay>()

    private val quotes = listOf(
        "오늘 포기하면, 내일도 똑같은 이유로 포기하게 된다.",
        "작은 꾸준함이 큰 변화를 만든다.",
        "완벽보다 완료가 더 중요하다.",
        "하루 10분도 쌓이면 실력이다.",
        "지금 하는 게 결국 너를 만든다.",
        "의욕이 없을 때도, 습관은 너를 움직인다.",
        "오늘의 한 줄이 내일의 자신감을 만든다.",
        "시작이 반이고, 지속이 전부다.",
        "남과 비교하지 말고 어제의 나와 비교해라.",
        "할 수 있을 때 조금 더 해두자."
    )
    private var quoteIndex = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_mypage, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvNickname = view.findViewById(R.id.tvNickname)
        tvProfileSub = view.findViewById(R.id.tvProfileSub)
        calendarView = view.findViewById(R.id.calendarView)

        // 1) 닉네임
        tvNickname.text = loadNicknameFromPrefs()

        // 2) 명언(✅ 5초로 변경)
        startQuoteTicker(periodMs = 5_000L)

        // 3) 달력: 일요일 decorator는 항상
        calendarView.addDecorator(SundayDecorator())

        // 4) 처음 진입한 달 공휴일 불러와서 표시
        val today = CalendarDay.today()
        loadHolidaysAndDecorate(today)

        // 5) 달 바뀌면 해당 달 공휴일 다시 불러오기
        calendarView.setOnMonthChangedListener { _, monthDay ->
            loadHolidaysAndDecorate(monthDay)
        }
    }

    private fun loadNicknameFromPrefs(): String {
        val prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return prefs.getString("nickname", "닉네임") ?: "닉네임"
    }

    private fun startQuoteTicker(periodMs: Long) {
        quoteIndex = 0
        tvProfileSub.text = quotes[quoteIndex]
        tvProfileSub.alpha = 0.7f

        quoteJob?.cancel()
        quoteJob = viewLifecycleOwner.lifecycleScope.launch {
            while (isActive) {
                delay(periodMs)
                showNextQuoteWithSlideUp()
            }
        }
    }

    private fun showNextQuoteWithSlideUp() {
        val slide = dp(10f)

        tvProfileSub.animate()
            .translationY(-slide)
            .alpha(0f)
            .setDuration(220L)
            .withEndAction {
                quoteIndex = (quoteIndex + 1) % quotes.size
                tvProfileSub.text = quotes[quoteIndex]

                tvProfileSub.translationY = slide
                tvProfileSub.animate()
                    .translationY(0f)
                    .alpha(0.7f)
                    .setDuration(260L)
                    .start()
            }
            .start()
    }

    private fun loadHolidaysAndDecorate(monthDay: CalendarDay) {
        // CalendarDay의 month는 라이브러리 구현마다 달라서(0/1-base 이슈) 안전하게 Calendar로 처리
        val cal = Calendar.getInstance().apply {
            time = monthDay.date // 일부 버전은 date(Date)가 있음
        }
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1 // 1~12

        viewLifecycleOwner.lifecycleScope.launch {
            val items = fetchHolidayItems(year, month)
            holidaySet.clear()
            holidaySet.addAll(items.mapNotNull { it.locdateToCalendarDayOrNull() })

            // ✅ 기존 HolidayDecorator만 갱신하려고 전체 decorators 초기화 후 다시 추가
            calendarView.removeDecorators()
            calendarView.addDecorator(SundayDecorator())
            calendarView.addDecorator(HolidayDecorator(holidaySet))

            calendarView.invalidateDecorators()
        }
    }

    // ---- 공휴일 API 호출 + JSON 파싱 ----

    private data class HolidayItem(
        val locdate: Int,      // yyyymmdd
        val dateName: String,
        val isHoliday: String  // "Y" or "N"
    )

    private suspend fun fetchHolidayItems(year: Int, month: Int): List<HolidayItem> {
        return try {
            val params = mapOf(
                "serviceKey" to SERVICE_KEY,
                "solYear" to year.toString(),
                "solMonth" to month.toString().padStart(2, '0'),
                "numOfRows" to "100",
                "pageNo" to "1"
            )

            val raw = PublicDataClient.api.getRestDeInfoRaw(params)
            parseHolidayJson(raw)
                .filter { it.isHoliday.equals("Y", ignoreCase = true) }
        } catch (e: Exception) {
            Log.e("HolidayAPI", "fetch failed", e)
            emptyList()
        }
    }

    private fun parseHolidayJson(raw: String): List<HolidayItem> {
        val root = JSONObject(raw)
        val response = root.getJSONObject("response")
        val body = response.optJSONObject("body") ?: return emptyList()
        val itemsObj = body.optJSONObject("items") ?: return emptyList()
        val itemAny = itemsObj.opt("item") ?: return emptyList()

        fun parseOne(o: JSONObject): HolidayItem {
            return HolidayItem(
                locdate = o.optInt("locdate"),
                dateName = o.optString("dateName"),
                isHoliday = o.optString("isHoliday", "N")
            )
        }

        val out = ArrayList<HolidayItem>()
        when (itemAny) {
            is JSONObject -> out.add(parseOne(itemAny))
            is JSONArray -> {
                for (i in 0 until itemAny.length()) {
                    out.add(parseOne(itemAny.getJSONObject(i)))
                }
            }
        }
        return out
    }

    private fun HolidayItem.locdateToCalendarDayOrNull(): CalendarDay? {
        if (locdate <= 0) return null
        val y = locdate / 10000
        val md = locdate % 10000
        val m = md / 100
        val d = md % 100

        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, y)
            set(Calendar.MONTH, m - 1)
            set(Calendar.DAY_OF_MONTH, d)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return CalendarDay.from(cal)
    }

    // ---- Decorators ----

    // 일요일 빨간색
    private class SundayDecorator : DayViewDecorator {
        override fun shouldDecorate(day: CalendarDay): Boolean {
            val cal = Calendar.getInstance().apply { time = day.date }
            return cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
        }

        override fun decorate(view: DayViewFacade) {
            view.addSpan(ForegroundColorSpan(Color.RED))
        }
    }

    // 공휴일 빨간색
    private class HolidayDecorator(
        private val holidays: Set<CalendarDay>
    ) : DayViewDecorator {

        override fun shouldDecorate(day: CalendarDay): Boolean = holidays.contains(day)

        override fun decorate(view: DayViewFacade) {
            view.addSpan(ForegroundColorSpan(Color.RED))
        }
    }

    private fun dp(value: Float): Float = value * resources.displayMetrics.density

    override fun onDestroyView() {
        super.onDestroyView()
        quoteJob?.cancel()
        quoteJob = null
    }
}
