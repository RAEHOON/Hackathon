package com.example.a20251215

import com.google.gson.annotations.SerializedName

data class SignupRequest(
    @SerializedName("username") val username: String,
    @SerializedName("nickname") val nickname: String,
    @SerializedName("loginid") val loginid: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)
