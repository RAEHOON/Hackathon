package com.example.a20251215.MypageFragment

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.example.a20251215.R
import com.example.a20251215.Retrofit.ApiResponse
import com.example.a20251215.Retrofit.RetrofitClient
import com.google.android.material.button.MaterialButton
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class EditPostDialogFragment : DialogFragment() {

    companion object {
        private const val ARG_POST_ID = "arg_post_id"
        private const val ARG_MEMBER_ID = "arg_member_id"
        private const val ARG_DATE = "arg_date"
        private const val ARG_TITLE = "arg_title"
        private const val ARG_CONTENT = "arg_content"
        private const val ARG_IMAGE_URL = "arg_image_url"

        private const val STATE_PICKED_URI = "state_picked_uri"

         private const val IMAGE_BASE = "https://www.maribot.monster"

        const val RESULT_KEY_EDIT_DONE = "result_edit_done"
        const val EXTRA_EDIT_DONE_ACTION = "edit_done_action"

        fun newInstance(
            postId: Int,
            memberId: Int,
            dateStr: String,
            title: String,
            content: String,
            imageUrl: String
        ): EditPostDialogFragment {
            return EditPostDialogFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_POST_ID, postId)
                    putInt(ARG_MEMBER_ID, memberId)
                    putString(ARG_DATE, dateStr)
                    putString(ARG_TITLE, title)
                    putString(ARG_CONTENT, content)
                    putString(ARG_IMAGE_URL, imageUrl)
                }
            }
        }
    }

    private var callUpdate: Call<ApiResponse>? = null

    private var pickedImageUri: Uri? = null
    private var originImageUrl: String = ""

    private fun resolveImageUrl(raw: String): String {
        val s = raw.trim()
        if (s.isBlank()) return ""
        return if (s.startsWith("http")) s else IMAGE_BASE + s
    }

    private fun showPreview(iv: ImageView) {
        // 1) 갤러리에서 새로 고른 게 있으면 그걸 우선 표시
        pickedImageUri?.let { uri ->
            Glide.with(this)
                .load(uri)
                .placeholder(R.drawable.error)
                .error(R.drawable.error2)
                .into(iv)
            return
        }

        // 2) 아니면 기존(원본) 이미지 표시
        val url = resolveImageUrl(originImageUrl)
        if (url.isNotBlank()) {
            Glide.with(this)
                .load(url)
                .placeholder(R.drawable.error)
                .error(R.drawable.error2)
                .into(iv)
        } else {
            // 원본도 없을 때(이미지 없는 글) 기본 처리
            iv.setImageResource(R.drawable.error2)
        }
    }

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (!isAdded) return@registerForActivityResult
            uri?.let {
                pickedImageUri = it
                val iv = view?.findViewById<ImageView>(R.id.ivEditPhoto)
                if (iv != null) showPreview(iv)
                view?.findViewById<MaterialButton>(R.id.btnPickImage)?.text = "사진 변경"
            }
        }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawableResource(android.R.color.transparent)
            setDimAmount(0.65f)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.framgment_mapage_calendar_edit_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val postId = requireArguments().getInt(ARG_POST_ID)
        val memberId = requireArguments().getInt(ARG_MEMBER_ID)
        val dateStr = requireArguments().getString(ARG_DATE, "")
        val originTitle = requireArguments().getString(ARG_TITLE, "")
        val originContent = requireArguments().getString(ARG_CONTENT, "")
        originImageUrl = requireArguments().getString(ARG_IMAGE_URL, "")

         savedInstanceState?.getString(STATE_PICKED_URI)?.let {
            runCatching { Uri.parse(it) }.getOrNull()?.let { uri -> pickedImageUri = uri }
        }

        val etTitle = view.findViewById<EditText>(R.id.etTitle)
        val etContent = view.findViewById<EditText>(R.id.etContent)
        val ivEditPhoto = view.findViewById<ImageView>(R.id.ivEditPhoto)

        val btnClose = view.findViewById<ImageButton>(R.id.btnClose)
        val btnPickImage = view.findViewById<MaterialButton>(R.id.btnPickImage)
        val btnCancel = view.findViewById<MaterialButton>(R.id.btnCancel)
        val btnSave = view.findViewById<MaterialButton>(R.id.btnSave)
        val overlay = view.findViewById<View>(R.id.progressOverlay)

        etTitle.setText(originTitle)
        etContent.setText(originContent)

         showPreview(ivEditPhoto)
        btnPickImage.text = if (pickedImageUri != null) "사진 변경" else "사진 선택"

        fun setLoading(loading: Boolean) {
            overlay.visibility = if (loading) View.VISIBLE else View.GONE
            btnSave.isEnabled = !loading
            btnCancel.isEnabled = !loading
            btnPickImage.isEnabled = !loading
            btnClose.isEnabled = !loading
        }

        fun closeBoth(action: String) {
            parentFragmentManager.setFragmentResult(
                CertDetailDialogFragment.REQ_EDIT_POST,
                Bundle().apply { putString(CertDetailDialogFragment.EDIT_ACTION, action) }
            )
            parentFragmentManager.setFragmentResult(
                RESULT_KEY_EDIT_DONE,
                Bundle().apply { putString(EXTRA_EDIT_DONE_ACTION, action) }
            )
            dismissAllowingStateLoss()
        }

        btnClose.setOnClickListener { closeBoth("cancel") }
        btnCancel.setOnClickListener { closeBoth("cancel") }

        btnPickImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        btnSave.setOnClickListener {
            val title = etTitle.text?.toString()?.trim().orEmpty()
            val content = etContent.text?.toString()?.trim().orEmpty()

            if (title.isBlank() || content.isBlank()) {
                Toast.makeText(requireContext(), "제목과 내용을 모두 입력하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            setLoading(true)

            val postIdPart = postId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val memberIdPart = memberId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val titlePart = title.toRequestBody("text/plain".toMediaTypeOrNull())
            val contentPart = content.toRequestBody("text/plain".toMediaTypeOrNull())

            var imagePart: MultipartBody.Part? = null
            pickedImageUri?.let { uri ->
                try {
                    val inputStream = requireContext().contentResolver.openInputStream(uri)
                    val tempFile = File.createTempFile("upload_", ".jpg", requireContext().cacheDir)
                    tempFile.outputStream().use { output -> inputStream?.copyTo(output) }

                    val requestFile = tempFile.asRequestBody("image/*".toMediaTypeOrNull())
                    imagePart = MultipartBody.Part.createFormData("image", tempFile.name, requestFile)
                } catch (e: Exception) {
                    setLoading(false)
                    Toast.makeText(requireContext(), "이미지 처리 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            callUpdate?.cancel()
            callUpdate = RetrofitClient.apiService.updatePost(
                postIdPart,
                memberIdPart,
                titlePart,
                contentPart,
                imagePart
            )

            callUpdate?.enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    setLoading(false)
                    if (!isAdded) return

                    val body = response.body()
                    if (!response.isSuccessful || body == null) {
                        Toast.makeText(requireContext(), "수정 실패 (HTTP ${response.code()})", Toast.LENGTH_SHORT).show()
                        return
                    }

                    if (!body.success) {
                        Toast.makeText(requireContext(), body.message, Toast.LENGTH_SHORT).show()
                        return
                    }

                    Toast.makeText(requireContext(), "수정 완료!", Toast.LENGTH_SHORT).show()

                    parentFragmentManager.setFragmentResult(
                        CertDetailDialogFragment.RESULT_KEY_POST_CHANGED,
                        Bundle().apply {
                            putString(CertDetailDialogFragment.EXTRA_ACTION, "updated")
                            putString(CertDetailDialogFragment.EXTRA_DATE, dateStr)
                            putInt(CertDetailDialogFragment.EXTRA_POST_ID, postId)
                        }
                    )

                    closeBoth("saved")
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    if (!isAdded) return
                    setLoading(false)
                    Toast.makeText(requireContext(), "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(STATE_PICKED_URI, pickedImageUri?.toString())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        callUpdate?.cancel()
        callUpdate = null
    }
}
