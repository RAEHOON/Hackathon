package com.example.a20251215.MypageFragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.a20251215.R
import com.example.a20251215.holiday.KasiRetrofit
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate

class MypageFragment : Fragment() {

    companion object {
        private const val TAG = "MYPAGE"
        private const val PREF_NAME = "UserInfo"
        private const val KEY_NICKNAME = "nickname"
    }

    private var quoteJob: Job? = null

    private lateinit var tvNickname: TextView
    private lateinit var tvProfileSub: TextView
    private lateinit var calendarView: MaterialCalendarView

    private val KASI_SERVICE_KEY =
        "2b5ae7ad6f3c0c0d60f341541b0bd15c3a99e6a0c7145f7719d730a5726ffbeb"

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
    ): View {
        return inflater.inflate(R.layout.fragment_mypage, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvNickname = view.findViewById(R.id.tvNickname)
        tvProfileSub = view.findViewById(R.id.tvProfileSub)
        calendarView = view.findViewById(R.id.calendarView)

        Log.d(TAG, "onViewCreated() called")

        val nick = loadNicknameFromPrefs()
        tvNickname.text = nick
        Log.d(TAG, "tvNickname set to '$nick'")

        startQuoteTicker_5sec()

        val nowLocal: LocalDate = calendarView.currentDate.date
        loadAndDecorateMonth(nowLocal.year, nowLocal.monthValue)

        calendarView.setOnMonthChangedListener { _, day ->
            val local: LocalDate = day.date
            loadAndDecorateMonth(local.year, local.monthValue)
        }

        calendarView.setOnDateChangedListener { _, day, _ ->
            val local: LocalDate = day.date
            Toast.makeText(
                requireContext(),
                "${local.year}-${local.monthValue}-${local.dayOfMonth}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun loadNicknameFromPrefs(): String {
        val prefs = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val nick = prefs.getString(KEY_NICKNAME, null)

        Log.d(TAG, "loadNicknameFromPrefs: file=$PREF_NAME nickname='$nick'")
        Log.d(TAG, "prefs all = ${prefs.all}")

        return nick ?: "닉네임"
    }

    private fun startQuoteTicker_5sec() {
        quoteIndex = 0
        tvProfileSub.text = quotes[quoteIndex]
        tvProfileSub.alpha = 0.7f

        quoteJob?.cancel()
        quoteJob = viewLifecycleOwner.lifecycleScope.launch {
            while (isActive) {
                delay(5_000L)
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

    private fun loadAndDecorateMonth(year: Int, month1to12: Int) {
        applyDecorators(year, month1to12, emptySet())

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val monthStr = String.format("%02d", month1to12)

                val res = KasiRetrofit.api.getRestDeInfo(
                    serviceKey = KASI_SERVICE_KEY,
                    solYear = year.toString(),
                    solMonth = monthStr
                )

                if (!res.isSuccessful) return@launch
                val xml = res.body().orEmpty()
                if (xml.isBlank()) return@launch

                val holidays: Set<CalendarDay> = HolidayXmlParser.parse(xml)
                    .mapNotNull { item ->
                        val raw = item.yyyymmdd
                        if (raw.length != 8) return@mapNotNull null

                        val y = raw.substring(0, 4).toIntOrNull() ?: return@mapNotNull null
                        val m = raw.substring(4, 6).toIntOrNull() ?: return@mapNotNull null
                        val d = raw.substring(6, 8).toIntOrNull() ?: return@mapNotNull null

                        val local = LocalDate.of(y, m, d)
                        CalendarDay.from(local)
                    }
                    .toSet()

                applyDecorators(year, month1to12, holidays)
            } catch (e: Exception) {
                Log.e(TAG, "loadAndDecorateMonth error: ${e.message}", e)
            }
        }
    }

    private fun applyDecorators(year: Int, month1to12: Int, holidays: Set<CalendarDay>) {
        calendarView.removeDecorators()
        calendarView.addDecorator(SundayDecorator())

        if (holidays.isNotEmpty()) {
            calendarView.addDecorator(HolidayDecorator(holidays))
        }

        calendarView.invalidateDecorators()
    }

    private fun dp(value: Float): Float = value * resources.displayMetrics.density

    override fun onResume() {
        super.onResume()

         if (!::tvNickname.isInitialized) return

        val nick = loadNicknameFromPrefs()
        tvNickname.text = nick
        Log.d(TAG, "onResume() updated nickname='$nick'")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        quoteJob?.cancel()
        quoteJob = null
    }
}
