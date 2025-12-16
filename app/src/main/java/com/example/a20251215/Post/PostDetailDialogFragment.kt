package com.example.a20251215.Post

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.a20251215.R

import com.example.a20251215.Retrofit.RetrofitClient

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.let
import kotlin.text.contains
import kotlin.text.isNotEmpty
import kotlin.text.substringAfter
import kotlin.text.substringBefore

class PostDetailDialogFragment : DialogFragment() {

    companion object {
        private const val ARG_POST_ID = "post_id"

        fun newInstance(postId: Int): PostDetailDialogFragment {
            val fragment = PostDetailDialogFragment()
            val args = Bundle()
            args.putInt(ARG_POST_ID, postId)
            fragment.arguments = args
            return fragment
        }
    }

    private var currentPostId: Int = -1
    private var currentMemberId: Int = -1

    private lateinit var tvTitle: TextView
    private lateinit var tvNickname: TextView
    private lateinit var tvDate: TextView
    private lateinit var ivImage: ImageView
    private lateinit var tvContent: TextView

    private lateinit var layoutActions: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_Black_NoTitleBar_Fullscreen)

        arguments?.let {
            currentPostId = it.getInt(ARG_POST_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_post_detail, container, false)

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.BLACK))
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvTitle = view.findViewById(R.id.tv_detail_title)
        tvNickname = view.findViewById(R.id.tv_detail_nickname)
        tvDate = view.findViewById(R.id.tv_detail_date)
        ivImage = view.findViewById(R.id.iv_detail_image)
        tvContent = view.findViewById(R.id.tv_detail_content)
        layoutActions = view.findViewById(R.id.layout_author_actions)

        val btnBack = view.findViewById<ImageView>(R.id.btn_back)
        val btnEdit = view.findViewById<TextView>(R.id.btn_edit)
        val btnDelete = view.findViewById<TextView>(R.id.btn_delete)

        btnBack.setOnClickListener { dismiss() } // 다이얼로그 닫기

        val sharedPref = requireActivity().getSharedPreferences("UserInfo", Context.MODE_PRIVATE)
        currentMemberId = sharedPref.getInt("member_id", -1)

        if (currentPostId != -1) {
            getPostDetails(currentPostId)
        } else {
            Toast.makeText(context, "게시글 ID가 없습니다.", Toast.LENGTH_SHORT).show()
            dismiss()
        }

        btnEdit.setOnClickListener {
            Toast.makeText(context, "수정 기능 준비 중", Toast.LENGTH_SHORT).show()
        }

        btnDelete.setOnClickListener {
            Toast.makeText(context, "삭제 기능 준비 중", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getPostDetails(postId: Int) {
        RetrofitClient.apiService.getPostDetail(postId).enqueue(object : Callback<PostDetailResponse> {
            override fun onResponse(call: Call<PostDetailResponse>, response: Response<PostDetailResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    val post = response.body()?.data

                    if (post != null) {
                        displayPost(post)

                        if (post.memberId == currentMemberId) {
                            layoutActions.visibility = View.VISIBLE
                        }
                    }
                } else {
                    Toast.makeText(context, "게시글을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                    dismiss()
                }
            }

            override fun onFailure(call: Call<PostDetailResponse>, t: Throwable) {
                Log.e("PostDetail", "통신 오류: ${t.message}")
                Toast.makeText(context, "서버 연결 실패", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun displayPost(post: PostDetail) {
        tvTitle.text = post.title
        tvNickname.text = post.nickname
        tvDate.text = post.createdAt.substringBefore(" ")
        tvContent.text = post.content

        post.imageUrl?.let { base64String ->
            if (base64String.isNotEmpty()) {
                try {
                    val pureBase64 = if (base64String.contains(",")) {
                        base64String.substringAfter(",")
                    } else {
                        base64String
                    }

                    val decodedBytes = Base64.decode(pureBase64, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

                    ivImage.setImageBitmap(bitmap)
                    ivImage.visibility = View.VISIBLE
                } catch (e: Exception) {
                    Log.e("Base64Decode", "Base64 디코딩 실패: ${e.message}")
                    ivImage.visibility = View.GONE
                }
            }
        }
    }
}