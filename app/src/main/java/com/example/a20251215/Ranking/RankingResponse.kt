package com.example.a20251215.Ranking

import com.google.gson.annotations.SerializedName

data class RankingResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: List<RankingItem>
)

data class RankingItem(
    @SerializedName("member_id") val memberId: Int,
    @SerializedName("nickname") val nickname: String,
    @SerializedName("upload_count") val uploadCount: Int
)
