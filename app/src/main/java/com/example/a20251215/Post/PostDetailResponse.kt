package com.example.a20251215.Post

import com.google.gson.annotations.SerializedName

data class PostDetailResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: PostDetail
)

data class PostDetail(
    @SerializedName("post_id") val postId: Int,
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("member_id") val memberId: Int,
    @SerializedName("nickname") val nickname: String,
    @SerializedName("login_id") val loginId: String
)
