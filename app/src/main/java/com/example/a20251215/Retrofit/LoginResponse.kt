package com.example.a20251215

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: LoginUserData?
)

data class LoginUserData(
    @SerializedName("loginid") val loginid: String,
    @SerializedName("email") val email: String,
    @SerializedName("username") val username: String,
    @SerializedName("nickname") val nickname: String
)
