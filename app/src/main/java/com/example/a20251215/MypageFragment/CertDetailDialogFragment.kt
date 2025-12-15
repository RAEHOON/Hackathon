package com.example.a20251215.MypageFragment

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import com.example.a20251215.Post.Post
import com.example.a20251215.Post.PostListResponse
import com.example.a20251215.R
import com.example.a20251215.Retrofit.ApiResponse
import com.example.a20251215.Retrofit.RetrofitClient
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.threeten.bp.LocalDate
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CertDetailDialogFragment : DialogFragment() {

    companion object {
        private const val TAG = "CertDetailDialog"

        private const val ARG_TARGET_USER_ID = "targetUserId"
        private const val ARG_MY_USER_ID = "myUserId"
        private const val ARG_DATE = "date" // yyyy-MM-dd

        const val RESULT_KEY_POST_CHANGED = "result_post_changed"
        const val EXTRA_ACTION = "action" // "updated" | "deleted"
        const val EXTRA_DATE = "date"
        const val EXTRA_POST_ID = "post_id"

         const val REQ_EDIT_POST = "req_edit_post"
        const val EDIT_ACTION = "edit_action" // "cancel" | "saved"

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

    private var callList: Call<PostListResponse>? = null
    private var callDelete: Call<ApiResponse>? = null

    private var currentPost: Post? = null

    private var cachedMemberId: Int = -1
    private var cachedDateStr: String = ""
    private var cachedIsOwner: Boolean = false

    private var tvTitle: TextView? = null
    private var tvDate: TextView? = null
    private var tvContent: TextView? = null
    private var ivPhoto: ImageView? = null
    private var layoutActions: LinearLayout? = null
    private var btnEdit: View? = null
    private var btnDelete: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, 0)
        Log.d(TAG, "onCreate()")
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setDimAmount(0.65f)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.framgment_mapage_calendar_dialogl, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val targetUserId = requireArguments().getInt(ARG_TARGET_USER_ID, -1)
        val myUserId = requireArguments().getInt(ARG_MY_USER_ID, -1)
        val dateStr = requireArguments().getString(ARG_DATE) ?: LocalDate.now().toString()
        val isOwner = (targetUserId == myUserId)

        cachedMemberId = targetUserId // owner면 myUserId랑 같음
        cachedDateStr = dateStr
        cachedIsOwner = isOwner

        val prefsAll = requireContext()
            .getSharedPreferences("UserInfo", Context.MODE_PRIVATE)
            .all
        Log.d(TAG, "open: targetUserId=$targetUserId myUserId=$myUserId date=$dateStr isOwner=$isOwner")
        Log.d(TAG, "prefs snapshot(UserInfo)=$prefsAll")

        tvTitle = view.findViewById(R.id.tvTitle)
        tvDate = view.findViewById(R.id.tvDate)
        tvContent = view.findViewById(R.id.tvContent)
        ivPhoto = view.findViewById(R.id.ivPhoto)

        val btnClose = view.findViewById<ImageButton>(R.id.btnClose)
        layoutActions = view.findViewById(R.id.layoutActions)
        btnEdit = view.findViewById(R.id.btnEdit)
        btnDelete = view.findViewById(R.id.btnDelete)

        tvDate?.text = dateStr
        tvTitle?.text = "제목"
        tvContent?.text = "불러오는 중..."
        ivPhoto?.setImageDrawable(null)

        layoutActions?.isVisible = isOwner
        setActionsEnabled(btnEdit, btnDelete, enabled = false)

         parentFragmentManager.setFragmentResultListener(REQ_EDIT_POST, viewLifecycleOwner) { _, b ->
            val action = b.getString(EDIT_ACTION, "")
            Log.d(TAG, "REQ_EDIT_POST received: action=$action -> close detail dialog")
            if (action == "cancel" || action == "saved") {
                dismissAllowingStateLoss()
            }
        }

         parentFragmentManager.setFragmentResultListener(
            EditPostDialogFragment.RESULT_KEY_EDIT_DONE,
            viewLifecycleOwner
        ) { _, b ->
            val action = b.getString(EditPostDialogFragment.EXTRA_EDIT_DONE_ACTION, "")
            Log.d(TAG, "RESULT_KEY_EDIT_DONE received: action=$action -> close detail dialog")
            if (action == "cancel" || action == "saved") {
                dismissAllowingStateLoss()
            }
        }

        btnClose.setOnClickListener { dismissAllowingStateLoss() }

        btnEdit?.setOnClickListener {
            if (!cachedIsOwner) return@setOnClickListener
            val post = currentPost ?: return@setOnClickListener

            EditPostDialogFragment.newInstance(
                postId = post.postId,
                memberId = cachedMemberId,
                dateStr = cachedDateStr,
                title = post.title,
                content = post.content,
                imageUrl = post.imageUrl ?: ""
            ).show(parentFragmentManager, "EditPostDialog")
        }

        btnDelete?.setOnClickListener {
            if (!cachedIsOwner) return@setOnClickListener
            val post = currentPost ?: return@setOnClickListener

            MaterialAlertDialogBuilder(requireContext())
                .setTitle("삭제할까요?")
                .setMessage("이 게시글은 바로 삭제됩니다.")
                .setNegativeButton("취소", null)
                .setPositiveButton("삭제") { _, _ ->
                    requestDeletePost(postId = post.postId, memberId = cachedMemberId)
                }
                .show()
        }

        loadPostByDate_usingGetMyPosts(
            memberId = targetUserId,
            dateStr = dateStr,
            isOwner = isOwner
        )
    }

    private fun loadPostByDate_usingGetMyPosts(memberId: Int, dateStr: String, isOwner: Boolean) {
        currentPost = null
        tvContent?.text = "불러오는 중..."
        ivPhoto?.setImageDrawable(null)

        if (memberId <= 0) {
            tvContent?.text = "유저 정보(member_id)가 없어서 불러올 수 없어요"
            applyNoPostUi(isOwner)
            return
        }

        if (isOwner) {
            layoutActions?.isVisible = true
            setActionsEnabled(btnEdit, btnDelete, enabled = false)
        } else {
            layoutActions?.isVisible = false
        }

        callList?.cancel()
        callList = RetrofitClient.apiService.getMyPosts(memberId)

        callList?.enqueue(object : Callback<PostListResponse> {
            override fun onResponse(call: Call<PostListResponse>, response: Response<PostListResponse>) {
                if (!isAdded) return
                val body = response.body()

                if (!response.isSuccessful || body == null) {
                    tvContent?.text = "불러오기 실패 (HTTP ${response.code()})"
                    applyNoPostUi(isOwner)
                    return
                }

                if (!body.success) {
                    tvContent?.text = body.message
                    applyNoPostUi(isOwner)
                    return
                }

                val picked = pickPostByDate(body.data, dateStr)
                if (picked == null) {
                    tvContent?.text = "이 날짜에 인증한 게시글이 없어요 🙂"
                    applyNoPostUi(isOwner)
                    return
                }

                currentPost = picked
                tvTitle?.text = picked.title
                tvContent?.text = picked.content
                renderImage(picked.imageUrl)

                if (isOwner) {
                    layoutActions?.isVisible = true
                    setActionsEnabled(btnEdit, btnDelete, enabled = true)
                } else {
                    layoutActions?.isVisible = false
                }
            }

            override fun onFailure(call: Call<PostListResponse>, t: Throwable) {
                if (!isAdded) return
                tvContent?.text = "네트워크 오류: ${t.message ?: "unknown"}"
                applyNoPostUi(isOwner)
            }
        })
    }

    private fun requestDeletePost(postId: Int, memberId: Int) {
        if (memberId <= 0) {
            Toast.makeText(requireContext(), "member_id가 없어서 삭제할 수 없어요", Toast.LENGTH_SHORT).show()
            return
        }

        setActionsEnabled(btnEdit, btnDelete, enabled = false)

        callDelete?.cancel()
        callDelete = RetrofitClient.apiService.deletePost(postId = postId, memberId = memberId)

        callDelete?.enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (!isAdded) return
                val body = response.body()

                if (!response.isSuccessful || body == null) {
                    Toast.makeText(requireContext(), "삭제 실패 (HTTP ${response.code()})", Toast.LENGTH_SHORT).show()
                    setActionsEnabled(btnEdit, btnDelete, enabled = true)
                    return
                }

                if (!body.success) {
                    Toast.makeText(requireContext(), body.message ?: "삭제 실패", Toast.LENGTH_SHORT).show()
                    setActionsEnabled(btnEdit, btnDelete, enabled = true)
                    return
                }

                Toast.makeText(requireContext(), "삭제 완료!", Toast.LENGTH_SHORT).show()
                sendChangedResult(action = "deleted", postId = postId)
                dismissAllowingStateLoss()
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                if (!isAdded) return
                Toast.makeText(requireContext(), "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                setActionsEnabled(btnEdit, btnDelete, enabled = true)
            }
        })
    }

    private fun sendChangedResult(action: String, postId: Int) {
        val b = Bundle().apply {
            putString(EXTRA_ACTION, action)
            putString(EXTRA_DATE, cachedDateStr)
            putInt(EXTRA_POST_ID, postId)
        }
        parentFragmentManager.setFragmentResult(RESULT_KEY_POST_CHANGED, b)
    }

    private fun applyNoPostUi(isOwner: Boolean) {
        currentPost = null
        ivPhoto?.setImageDrawable(null)
        tvTitle?.text = "제목"

        if (isOwner) {
            layoutActions?.isVisible = true
            setActionsEnabled(btnEdit, btnDelete, enabled = false)
        } else {
            layoutActions?.isVisible = false
        }
    }

    private fun pickPostByDate(list: List<Post>, dateStr: String): Post? {
        val matched = list.filter { p ->
            val created = p.createdAt.trim()
            val day = if (created.length >= 10) created.substring(0, 10) else created
            day == dateStr
        }
        return matched.maxByOrNull { it.createdAt }
    }

    private fun setActionsEnabled(edit: View?, del: View?, enabled: Boolean) {
        edit?.isEnabled = enabled
        del?.isEnabled = enabled
        val alpha = if (enabled) 1.0f else 0.35f
        edit?.alpha = alpha
        del?.alpha = alpha
    }

    private fun renderImage(imageUrl: String?) {
        val iv = ivPhoto ?: return
        val url = imageUrl?.trim().orEmpty()
        if (url.isBlank()) {
            iv.setImageDrawable(null)
            return
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        callList?.cancel()
        callDelete?.cancel()
        callList = null
        callDelete = null
        currentPost = null
    }
}
