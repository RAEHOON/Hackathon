package com.example.a20251215

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
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.example.a20251215.Post.PostDetailResponse
import com.example.a20251215.R
import com.example.a20251215.Retrofit.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomePostDetailDialogFragment : DialogFragment() {

    companion object {
        private const val ARG_POST_ID = "post_id"

        fun newInstance(postId: Int): HomePostDetailDialogFragment {
            val fragment = HomePostDetailDialogFragment()
            val args = Bundle()
            args.putInt(ARG_POST_ID, postId)
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var tvTitle: TextView
    private lateinit var tvDate: TextView
    private lateinit var tvContent: TextView
    private lateinit var ivPhoto: ImageView
    private lateinit var btnClose: ImageButton
    private lateinit var layoutActions: LinearLayout
    private lateinit var btnEdit: View
    private lateinit var btnDelete: View

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
    ): View? {
        return inflater.inflate(R.layout.dialog_home_post_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvTitle = view.findViewById(R.id.tvTitle)
        tvDate = view.findViewById(R.id.tvDate)
        tvContent = view.findViewById(R.id.tvContent)
        ivPhoto = view.findViewById(R.id.ivPhoto)
        btnClose = view.findViewById(R.id.btnClose)
        layoutActions = view.findViewById(R.id.layoutActions)
        btnEdit = view.findViewById(R.id.btnEdit)
        btnDelete = view.findViewById(R.id.btnDelete)

        layoutActions.visibility = View.GONE  // Home에서는 수정/삭제 안보여줌

        btnClose.setOnClickListener {
            dismissAllowingStateLoss()
        }

        val postId = arguments?.getInt(ARG_POST_ID, -1) ?: -1
        if (postId != -1) {
            fetchPostDetail(postId)
        } else {
            Toast.makeText(context, "postId가 잘못되었습니다.", Toast.LENGTH_SHORT).show()
            dismissAllowingStateLoss()
        }
    }

    private fun fetchPostDetail(postId: Int) {
        RetrofitClient.apiService.getPostDetail(postId).enqueue(object : Callback<PostDetailResponse> {
            override fun onResponse(
                call: Call<PostDetailResponse>,
                response: Response<PostDetailResponse>
            ) {
                if (response.isSuccessful && response.body()?.success == true) {
                    val post = response.body()?.data ?: return
                    tvTitle.text = post.title
                    tvDate.text = post.createdAt.substring(0, 10)
                    tvContent.text = post.content

                    val imageUrl = post.imageUrl?.trim().orEmpty()
                    if (imageUrl.isNotBlank()) {
                        val fullUrl = "https://www.maribot.monster$imageUrl"
                        Glide.with(this@HomePostDetailDialogFragment)
                            .load(fullUrl)
                            .placeholder(R.drawable.error)
                            .error(R.drawable.error2)
                            .into(ivPhoto)
                    } else {
                        ivPhoto.setImageDrawable(null)
                    }
                } else {
                    Toast.makeText(context, "게시글 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
                    dismissAllowingStateLoss()
                }
            }

            override fun onFailure(call: Call<PostDetailResponse>, t: Throwable) {
                Toast.makeText(context, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                dismissAllowingStateLoss()
            }
        })
    }
}