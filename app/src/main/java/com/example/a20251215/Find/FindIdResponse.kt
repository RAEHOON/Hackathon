package com.example.a20251215.Find

import com.google.gson.annotations.SerializedName

data class FindIdResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("loginid") val loginid: String? // 성공 시에만 존재
)
