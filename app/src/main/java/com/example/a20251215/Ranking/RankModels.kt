package com.example.a20251215.Ranking

 enum class RankType { BEST, WORST }

 data class UserScore(
    val userId: Int,
    val name: String,
    val certCount: Int
)

data class RankItem(
    val badge: String,
    val name: String,
    val count: String,
    val sub: String
)
