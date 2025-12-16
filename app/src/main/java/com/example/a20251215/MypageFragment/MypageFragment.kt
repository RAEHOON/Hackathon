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
import com.example.a20251215.Post.PostListResponse
import com.example.a20251215.R
import com.example.a20251215.Retrofit.RetrofitClient
import com.example.a20251215.holiday.KasiRetrofit
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MypageFragment : Fragment() {

    companion object {
        private const val TAG = "MYPAGE"

        private const val PREF_NAME = "UserInfo"
        private const val KEY_NICKNAME = "nickname"

        private const val KEY_MEMBER_ID = "member_id"
        private const val KEY_USER_ID_1 = "user_id"
        private const val KEY_USER_ID_2 = "id"

        private const val ARG_TARGET_USER_ID = "targetUserId"
        private const val DIALOG_TAG = "cert_detail"
    }

    private var quoteJob: Job? = null

    private lateinit var tvNickname: TextView
    private lateinit var tvProfileSub: TextView
    private lateinit var calendarView: MaterialCalendarView

    private lateinit var tvStat1: TextView
    private lateinit var tvStat2: TextView
    private lateinit var tvStat3: TextView

    private var callPosts: Call<PostListResponse>? = null
    private var cachedHolidayDays: Set<CalendarDay> = emptySet()
    private var cachedCertDays: Set<CalendarDay> = emptySet()

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
    ): View = inflater.inflate(R.layout.fragment_mypage, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvNickname = view.findViewById(R.id.tvNickname)
        tvProfileSub = view.findViewById(R.id.tvProfileSub)
        calendarView = view.findViewById(R.id.calendarView)

        tvStat1 = view.findViewById(R.id.tvStat1)
        tvStat2 = view.findViewById(R.id.tvStat2)
        tvStat3 = view.findViewById(R.id.tvStat3)

        val myUserId = loadMyUserIdFromPrefs()

        val argTarget = arguments?.getInt(ARG_TARGET_USER_ID, -1) ?: -1
        val targetUserId = if (argTarget > 0) argTarget else myUserId

        tvNickname.text = loadNicknameFromPrefs()
        startQuoteTicker_5sec()

         parentFragmentManager.setFragmentResultListener(
            CertDetailDialogFragment.RESULT_KEY_POST_CHANGED,
            viewLifecycleOwner
        ) { _, _ ->
            val cur = calendarView.currentDate.date
            refreshMonthAll(myUserId, cur.year, cur.monthValue)
        }

        val nowLocal: LocalDate = calendarView.currentDate.date
        refreshMonthAll(myUserId, nowLocal.year, nowLocal.monthValue)

        calendarView.setOnMonthChangedListener { _, day ->
            val local: LocalDate = day.date
            refreshMonthAll(myUserId, local.year, local.monthValue)
        }

        calendarView.setOnDateChangedListener { _, day, _ ->
            val local: LocalDate = day.date

            if (myUserId <= 0) {
                Toast.makeText(requireContext(), "로그인 정보(member_id)가 없어요", Toast.LENGTH_SHORT).show()
                return@setOnDateChangedListener
            }

            val realTarget = if (targetUserId > 0) targetUserId else myUserId
            showCertDialog(realTarget, myUserId, local)
        }
    }

    private fun refreshMonthAll(memberId: Int, year: Int, month1to12: Int) {
        loadAndDecorateMonth(year, month1to12)           // 공휴일
        loadCertDaysAndTopStats(memberId, year, month1to12) // 인증 점 + 상단 통계
    }

    private fun showCertDialog(targetUserId: Int, myUserId: Int, date: LocalDate) {
        if (parentFragmentManager.findFragmentByTag(DIALOG_TAG) != null) return

        CertDetailDialogFragment
            .newInstance(targetUserId = targetUserId, myUserId = myUserId, date = date)
            .show(parentFragmentManager, DIALOG_TAG)
    }

     private fun loadCertDaysAndTopStats(memberId: Int, year: Int, month1to12: Int) {
        if (memberId <= 0) return

        callPosts?.cancel()
        callPosts = RetrofitClient.apiService.getMyPosts(memberId)

        callPosts?.enqueue(object : Callback<PostListResponse> {
            override fun onResponse(call: Call<PostListResponse>, response: Response<PostListResponse>) {
                if (!isAdded) return

                val body = response.body()
                if (!response.isSuccessful || body == null || !body.success) {
                    setTopStats(monthCount = 0, streak = 0)
                    cachedCertDays = emptySet()
                    applyDecoratorsAll()
                    return
                }

                 val allDates: Set<LocalDate> = body.data.mapNotNull { p ->
                    parseDate(p.createdAt)
                }.toSet()

                 val monthDates: Set<LocalDate> = allDates.filter { d ->
                    d.year == year && d.monthValue == month1to12
                }.toSet()

                val monthCount = monthDates.size

                 val streak = computeStreakFromLast(allDates)

                setTopStats(monthCount = monthCount, streak = streak)

                 cachedCertDays = monthDates.map { CalendarDay.from(it) }.toSet()
                applyDecoratorsAll()
            }

            override fun onFailure(call: Call<PostListResponse>, t: Throwable) {
                if (!isAdded) return
                Log.e(TAG, "loadCertDaysAndTopStats FAIL: ${t.message}", t)
                setTopStats(monthCount = 0, streak = 0)
                cachedCertDays = emptySet()
                applyDecoratorsAll()
            }
        })
    }

    private fun setTopStats(monthCount: Int, streak: Int) {
        tvStat1.text = "이번 달 ${monthCount}회"
        tvStat2.text = "연속 ${streak}일"
        tvStat3.text = "공부 양 :  ${rankLabel(monthCount)}"
    }

    private fun rankLabel(monthCount: Int): String {
        return when {
            monthCount <= 5 -> "조금 함"
            monthCount <= 10 -> "하긴 함"
            monthCount <= 20 -> "많이 함"
            else -> "LEGEND"
        }
    }

     private fun computeStreakFromLast(certDates: Set<LocalDate>): Int {
        if (certDates.isEmpty()) return 0
        var d = certDates.maxOrNull() ?: return 0
        var count = 0
        while (certDates.contains(d)) {
            count++
            d = d.minusDays(1)
        }
        return count
    }

    private fun parseDate(createdAt: String?): LocalDate? {
        val s = createdAt?.trim().orEmpty()
        if (s.length < 10) return null
        return try {
            LocalDate.parse(s.substring(0, 10))
        } catch (_: Exception) {
            null
        }
    }

     private fun loadAndDecorateMonth(year: Int, month1to12: Int) {
        cachedHolidayDays = emptySet()
        applyDecoratorsAll()

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

                        CalendarDay.from(LocalDate.of(y, m, d))
                    }
                    .toSet()

                cachedHolidayDays = holidays
                applyDecoratorsAll()
            } catch (e: Exception) {
                Log.e(TAG, "loadAndDecorateMonth error: ${e.message}", e)
            }
        }
    }

    private fun applyDecoratorsAll() {
        calendarView.removeDecorators()
        calendarView.addDecorator(SundayDecorator())

        if (cachedHolidayDays.isNotEmpty()) {
            calendarView.addDecorator(HolidayDecorator(cachedHolidayDays))
        }

         if (cachedCertDays.isNotEmpty()) {
            calendarView.addDecorator(
                PostMarkDecorator(
                    days = cachedCertDays,
                    dotRadiusPx = dp(2.8f),
                    color = android.graphics.Color.RED,
                    offsetXPx = dp(6.0f),
                    offsetYPx = dp(2.0f)
                )
            )
        }

        calendarView.invalidateDecorators()
    }

    private fun dp(value: Float): Float = value * resources.displayMetrics.density

    private fun loadNicknameFromPrefs(): String {
        val prefs = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_NICKNAME, null) ?: "닉네임"
    }

    private fun loadMyUserIdFromPrefs(): Int {
        val prefs = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        fun readIntOrString(key: String): Int {
            val vInt = prefs.getInt(key, Int.MIN_VALUE)
            if (vInt != Int.MIN_VALUE) return vInt

            val vStr = prefs.getString(key, null)?.toIntOrNull()
            if (vStr != null) return vStr

            return -1
        }

        readIntOrString(KEY_MEMBER_ID).takeIf { it > 0 }?.let { return it }
        readIntOrString(KEY_USER_ID_1).takeIf { it > 0 }?.let { return it }
        readIntOrString(KEY_USER_ID_2).takeIf { it > 0 }?.let { return it }

        return -1
    }

    private fun startQuoteTicker_5sec() {
        quoteIndex = 0
        tvProfileSub.text = quotes[quoteIndex]
        tvProfileSub.alpha = 0.7f
        tvProfileSub.translationY = 0f

        quoteJob?.cancel()
        quoteJob = viewLifecycleOwner.lifecycleScope.launch {
            while (isActive) {
                delay(5_000L)
                showNextQuoteWithSlideUp()
            }
        }
    }
    /** ✅ 뉴스/소식처럼: 위로 밀고(fade out) → 다음 문구를 아래에서 올림(fade in) */
    private fun showNextQuoteWithSlideUp() {
        val slide = dp(10f) // 올라가는 거리(원하면 8~14 정도로 조절)

        // 애니메이션 겹침 방지
        tvProfileSub.animate().cancel()

        tvProfileSub.animate()
            .translationY(-slide)
            .alpha(0f)
            .setDuration(220L)
            .withEndAction {
                // 다음 문구로 교체
                quoteIndex = (quoteIndex + 1) % quotes.size
                tvProfileSub.text = quotes[quoteIndex]

                // 아래에서 시작
                tvProfileSub.translationY = slide
                tvProfileSub.animate()
                    .translationY(0f)
                    .alpha(0.7f)
                    .setDuration(260L)
                    .start()
            }
            .start()
    }


        override fun onDestroyView() {
        super.onDestroyView()
        quoteJob?.cancel()
        quoteJob = null
        callPosts?.cancel()
        callPosts = null
    }
}
