package com.example.a20251215.MypageFragment

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import com.example.a20251215.R
import com.example.a20251215.Retrofit.ApiResponse
import com.example.a20251215.Retrofit.RetrofitClient
import com.google.android.material.button.MaterialButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream

class EditPostDialogFragment : DialogFragment() {

    companion object {
        private const val ARG_POST_ID = "arg_post_id"
        private const val ARG_MEMBER_ID = "arg_member_id"
        private const val ARG_DATE = "arg_date"
        private const val ARG_TITLE = "arg_title"
        private const val ARG_CONTENT = "arg_content"
        private const val ARG_IMAGE_URL = "arg_image_url"

        // (선택) 다른 방식 호환용 키
        const val RESULT_KEY_EDIT_DONE = "result_edit_done"
        const val EXTRA_EDIT_DONE_ACTION = "edit_done_action" // "cancel" | "saved"

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

     private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                pickedImageUri = uri
                view?.findViewById<ImageView>(R.id.ivEditPhoto)?.setImageURI(uri)
            }
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
         return inflater.inflate(R.layout.framgment_mapage_calendar_edit_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val postId = requireArguments().getInt(ARG_POST_ID, -1)
        val memberId = requireArguments().getInt(ARG_MEMBER_ID, -1)
        val dateStr = requireArguments().getString(ARG_DATE, "")
        val originTitle = requireArguments().getString(ARG_TITLE, "")
        val originContent = requireArguments().getString(ARG_CONTENT, "")
        originImageUrl = requireArguments().getString(ARG_IMAGE_URL, "")

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

            if (postId <= 0 || memberId <= 0) {
                Toast.makeText(requireContext(), "post_id/member_id가 이상해요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (title.isBlank()) {
                Toast.makeText(requireContext(), "제목을 입력해줘!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (content.isBlank()) {
                Toast.makeText(requireContext(), "내용을 입력해줘!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            setLoading(true)


            val imageUrlToSend = try {
                val uri = pickedImageUri
                if (uri != null) {
                    val bmp = decodeBitmap(uri)
                    bitmapToBase64Jpeg(bmp, quality = 80)
                } else {
                    originImageUrl
                }
            } catch (e: Exception) {
                setLoading(false)
                Toast.makeText(requireContext(), "이미지 처리 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            callUpdate?.cancel()
            callUpdate = RetrofitClient.apiService.updatePost(
                postId = postId,
                memberId = memberId,
                title = title,
                content = content,
                imageUrl = imageUrlToSend
            )

            callUpdate?.enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    if (!isAdded) return
                    val body = response.body()

                    if (!response.isSuccessful || body == null) {
                        setLoading(false)
                        Toast.makeText(requireContext(), "수정 실패 (HTTP ${response.code()})", Toast.LENGTH_SHORT).show()
                        return
                    }

                    if (!body.success) {
                        setLoading(false)
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

    private fun decodeBitmap(uri: Uri): Bitmap {
        return if (Build.VERSION.SDK_INT >= 28) {
            val source = ImageDecoder.createSource(requireContext().contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.isMutableRequired = false
            }
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
        }
    }

    private fun bitmapToBase64Jpeg(bitmap: Bitmap, quality: Int): String {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos)
        val bytes = baos.toByteArray()
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        callUpdate?.cancel()
        callUpdate = null
    }
}
