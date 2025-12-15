package com.example.a20251215.Post

import com.google.gson.annotations.SerializedName

data class Post(
    @SerializedName("post_id") val postId: Int,
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("created_at") val createdAt: String
)