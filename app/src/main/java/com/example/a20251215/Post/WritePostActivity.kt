package com.example.a20251215.Post

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.a20251215.R
import com.example.a20251215.Retrofit.ApiResponse
import com.example.a20251215.Retrofit.RetrofitClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class WritePostActivity : AppCompatActivity() {

    private var selectedImageUri: Uri? = null
    private lateinit var ivPreview: ImageView

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            ivPreview.visibility = View.VISIBLE
            ivPreview.setImageURI(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write_post)

        val etTitle = findViewById<EditText>(R.id.et_title)
        val etContent = findViewById<EditText>(R.id.et_content)
        val btnBack = findViewById<ImageView>(R.id.btn_back)
        val btnUpload = findViewById<TextView>(R.id.btn_upload)
        val btnImagePicker = findViewById<LinearLayout>(R.id.btn_image_picker)
        ivPreview = findViewById(R.id.iv_preview)

        btnBack.setOnClickListener { finish() }

        btnImagePicker.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        btnUpload.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val content = etContent.text.toString().trim()

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(this, "제목과 내용을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            uploadData(title, content)
        }
    }

    private fun uploadData(title: String, content: String) {
        val sharedPref = getSharedPreferences("UserInfo", Context.MODE_PRIVATE)
        val memberId = sharedPref.getInt("member_id", -1)

        if (memberId == -1) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val memberIdPart = memberId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val titlePart = title.toRequestBody("text/plain".toMediaTypeOrNull())
        val contentPart = content.toRequestBody("text/plain".toMediaTypeOrNull())

        var imagePart: MultipartBody.Part? = null

        selectedImageUri?.let { uri ->
            val inputStream = contentResolver.openInputStream(uri)
            val tempFile = File.createTempFile("upload_", ".jpg", cacheDir)
            tempFile.outputStream().use { output ->
                inputStream?.copyTo(output)
            }

            val requestFile = tempFile.asRequestBody("image/*".toMediaTypeOrNull())
            imagePart = MultipartBody.Part.createFormData("image", tempFile.name, requestFile)
        }

        RetrofitClient.apiService.uploadPost(
            memberIdPart,
            titlePart,
            contentPart,
            imagePart
        ).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(this@WritePostActivity, "업로드 성공!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    val msg = response.body()?.message ?: "서버 응답 없음"
                    val errorBody = response.errorBody()?.string()
                    Log.e("UploadFail", "Msg: $msg / ErrorBody: $errorBody")
                    Toast.makeText(this@WritePostActivity, "업로드 실패: $msg", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Toast.makeText(this@WritePostActivity, "통신 오류: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
