package com.example.a20251215.MypageFragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.a20251215.R
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MypageFragment : Fragment() {

    private var quoteJob: Job? = null

    private lateinit var tvNickname: TextView
    private lateinit var tvProfileSub: TextView

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

        tvNickname.text = loadNicknameFromPrefs()

        startQuoteTicker()
    }

    private fun loadNicknameFromPrefs(): String {
        val prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return prefs.getString("nickname", "닉네임") ?: "닉네임"
    }

    private fun startQuoteTicker() {
        quoteIndex = 0
        tvProfileSub.text = quotes[quoteIndex]

        tvProfileSub.alpha = 0.7f

        quoteJob?.cancel()
        quoteJob = viewLifecycleOwner.lifecycleScope.launch {
            while (isActive) {
                delay(10_000L)
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

    private fun dp(value: Float): Float {
        return value * resources.displayMetrics.density
    }

    override fun onDestroyView() {
        super.onDestroyView()
        quoteJob?.cancel()
        quoteJob = null
    }
}
