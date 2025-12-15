package com.example.a20251215



import com.google.gson.annotations.SerializedName

data class SignupResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String
)
