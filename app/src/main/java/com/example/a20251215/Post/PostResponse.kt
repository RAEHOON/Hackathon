package com.example.a20251215.Post
import com.google.gson.annotations.SerializedName

data class PostListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: List<Post>
)