package com.example.a20251215

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: LoginUserData?
)

data class LoginUserData(
    @SerializedName("member_id") val memberId: Int,
    @SerializedName("loginid") val loginId: String,
    @SerializedName("email") val email: String,
    @SerializedName("username") val username: String,
    @SerializedName("nickname") val nickname: String
)