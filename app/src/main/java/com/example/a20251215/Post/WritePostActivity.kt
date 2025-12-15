package com.example.a20251215.Post

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.InputStream
import kotlin.text.isEmpty
import kotlin.text.trim

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
        val sharedPref = getSharedPreferences("UserInfo", MODE_PRIVATE)
        val memberId = sharedPref.getInt("member_id", -1)

        if (memberId == -1) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        var imageString = ""
        if (selectedImageUri != null) {
            imageString = uriToBase64(selectedImageUri!!)
        }

        RetrofitClient.apiService.uploadPost(memberId, title, content, imageString)
            .enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(this@WritePostActivity, "업로드 성공!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        val msg = response.body()?.message ?: "서버 응답 없음"
                        val errorBody = response.errorBody()?.string()
                        Log.e("UploadFail", "Msg: $msg / ErrorBody: $errorBody")
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    Toast.makeText(this@WritePostActivity, "통신 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun uriToBase64(uri: Uri): String {
        return try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            // 1. 원본 비트맵 읽기
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap == null) return ""

            // 2. 이미지 리사이징 (최대 너비/높이를 1024px로 제한)
            val resizedBitmap = resizeBitmap(originalBitmap, 1024)

            // 3. 압축 및 Base64 변환
            val byteArrayOutputStream = ByteArrayOutputStream()
            // 화질 70%로 압축 (글자 수 줄이기 위함)
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()

            "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("ImageError", "이미지 변환 실패: ${e.message}")
            ""
        }
    }

    private fun resizeBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
        var width = bitmap.width
        var height = bitmap.height

        val bitmapRatio = width.toFloat() / height.toFloat()
        if (bitmapRatio > 1) {
            width = maxSize
            height = (width / bitmapRatio).toInt()
        } else {
            height = maxSize
            width = (height * bitmapRatio).toInt()
        }
        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }
}