package com.example.a20251215

import com.google.gson.annotations.SerializedName

data class SignupRequest(
    @SerializedName("name") val name: String,
    @SerializedName("nickname") val nickname: String,
    @SerializedName("userid") val userid: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)