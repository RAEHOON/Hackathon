package com.example.a20251215.MypageFragment

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.a20251215.R
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate

class CertDetailDialogFragment : DialogFragment() {

    companion object {
        private const val ARG_TARGET_USER_ID = "targetUserId"
        private const val ARG_MY_USER_ID = "myUserId"
        private const val ARG_DATE = "date" // yyyy-MM-dd

        fun newInstance(targetUserId: Int, myUserId: Int, date: LocalDate): CertDetailDialogFragment {
            return CertDetailDialogFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_TARGET_USER_ID, targetUserId)
                    putInt(ARG_MY_USER_ID, myUserId)
                    putString(ARG_DATE, date.toString())
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, 0)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setDimAmount(0.65f)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.framgment_mapage_calendar_dialogl, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val targetUserId = requireArguments().getInt(ARG_TARGET_USER_ID)
        val myUserId = requireArguments().getInt(ARG_MY_USER_ID)
        val dateStr = requireArguments().getString(ARG_DATE) ?: LocalDate.now().toString()
        val isOwner = (targetUserId == myUserId)

        val tvDate = view.findViewById<TextView>(R.id.tvDate)
        val tvContent = view.findViewById<TextView>(R.id.tvContent)
        val ivPhoto = view.findViewById<ImageView>(R.id.ivPhoto)

        val btnClose = view.findViewById<ImageButton>(R.id.btnClose)

        val layoutActions = view.findViewById<LinearLayout>(R.id.layoutActions)
        val btnEdit = view.findViewById<View>(R.id.btnEdit)
        val btnDelete = view.findViewById<View>(R.id.btnDelete)

        // ✅ 내 글이 아니면 수정/삭제 숨김
        layoutActions.isVisible = isOwner

        tvDate.text = dateStr
        tvContent.text = "불러오는 중..."

        btnClose.setOnClickListener { dismissAllowingStateLoss() }

        btnEdit.setOnClickListener { onEditClicked() }
        btnDelete.setOnClickListener { onDeleteClicked() }

        // ✅ 여기서 “해당 날짜 인증 게시글” 로드
        viewLifecycleOwner.lifecycleScope.launch {
            // TODO: 서버에서 targetUserId + dateStr 로 조회해서 바인딩
            tvContent.text = "여기에 인증 글 내용..."
            // Glide.with(ivPhoto).load(post.imageUrl).into(ivPhoto)
        }
    }

    private fun onEditClicked() {
        // TODO: EditActivity 열기
        dismissAllowingStateLoss()
    }

    private fun onDeleteClicked() {
        // TODO: 삭제 confirm + 서버 삭제 호출
        dismissAllowingStateLoss()
    }
}
