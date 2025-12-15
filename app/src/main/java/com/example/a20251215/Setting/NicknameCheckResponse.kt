package com.example.a20251215.Setting

import com.google.gson.annotations.SerializedName

data class NicknameCheckResponse(
    @SerializedName("exists") val exists: Boolean
)
