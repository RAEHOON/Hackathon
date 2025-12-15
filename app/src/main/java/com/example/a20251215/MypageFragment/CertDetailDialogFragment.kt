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
import com.example.a20251215.Post.Post
import com.example.a20251215.Post.PostListResponse
import com.example.a20251215.R
import com.example.a20251215.Retrofit.RetrofitClient
import org.threeten.bp.LocalDate
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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

    // ✅ companion object 안에 두면 안됨 (인스턴스 공유로 꼬임)
    private var call: Call<PostListResponse>? = null
    private var currentPost: Post? = null

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

        tvDate.text = dateStr
        tvContent.text = "불러오는 중..."
        ivPhoto.setImageDrawable(null)

        // ✅ 글 주인일 때만 "수정/삭제" 영역이 보일 수 있음 (글 없으면 아래에서 또 숨김)
        layoutActions.isVisible = isOwner

        btnClose.setOnClickListener { dismissAllowingStateLoss() }

        btnEdit.setOnClickListener {
            if (!isOwner) return@setOnClickListener
            val post = currentPost ?: return@setOnClickListener
            // TODO: 수정 화면 이동 (post.postId 넘기기)
            // startActivity(Intent(requireContext(), EditPostActivity::class.java).putExtra("post_id", post.postId))
            dismissAllowingStateLoss()
        }

        btnDelete.setOnClickListener {
            if (!isOwner) return@setOnClickListener
            val post = currentPost ?: return@setOnClickListener
            // TODO: 삭제 confirm + delete_post.php 호출 (post.postId, myUserId)
            dismissAllowingStateLoss()
        }

        // ✅ 서버에서 "해당 날짜 인증 게시글" 로드
        loadPostOfDate(
            targetUserId = targetUserId,
            dateStr = dateStr,
            isOwner = isOwner,
            tvContent = tvContent,
            ivPhoto = ivPhoto,
            layoutActions = layoutActions
        )
    }

    private fun loadPostOfDate(
        targetUserId: Int,
        dateStr: String, // yyyy-MM-dd
        isOwner: Boolean,
        tvContent: TextView,
        ivPhoto: ImageView,
        layoutActions: LinearLayout
    ) {
        currentPost = null
        tvContent.text = "불러오는 중..."
        ivPhoto.setImageDrawable(null)

        // ✅ 글 로드 전엔 숨겼다가, 성공+내글이면 보여주기 (버튼 사라진 원인 방지)
        layoutActions.isVisible = false

        call?.cancel()
        call = RetrofitClient.apiService.getUserPosts(targetUserId)

        call?.enqueue(object : Callback<PostListResponse> {
            override fun onResponse(call: Call<PostListResponse>, response: Response<PostListResponse>) {
                if (!isAdded) return

                if (!response.isSuccessful) {
                    tvContent.text = "불러오기 실패 (HTTP ${response.code()})"
                    layoutActions.isVisible = false
                    return
                }

                val body = response.body()
                if (body == null || !body.success) {
                    tvContent.text = body?.message ?: "불러오기 실패"
                    layoutActions.isVisible = false
                    return
                }

                val post = pickPostByDate(body.data, dateStr)
                if (post == null) {
                    tvContent.text = "이 날짜에 인증한 게시글이 없어요 🙂"
                    layoutActions.isVisible = false
                    return
                }

                currentPost = post
                tvContent.text = post.content

                // ✅ 이미지 로딩(원하면 Glide 추가해서 사용)
                // if (!post.imageUrl.isNullOrBlank()) {
                //     Glide.with(ivPhoto).load(post.imageUrl).into(ivPhoto)
                // }

                // ✅ 내 글 + 실제 글 있을 때만 수정/삭제 노출
                layoutActions.isVisible = isOwner
            }

            override fun onFailure(call: Call<PostListResponse>, t: Throwable) {
                if (!isAdded) return
                tvContent.text = "네트워크 오류: ${t.message ?: "unknown"}"
                layoutActions.isVisible = false
            }
        })
    }

    // created_at이 "YYYY-MM-DD HH:mm:ss" 형태라고 가정
    private fun pickPostByDate(list: List<Post>, dateStr: String): Post? {
        val filtered = list.filter { p ->
            p.createdAt.take(10) == dateStr
        }
        return filtered.maxByOrNull { it.createdAt } // 같은 날짜 여러개면 최신 1개
    }

    override fun onDestroyView() {
        super.onDestroyView()
        call?.cancel()
        call = null
        currentPost = null
    }
}
