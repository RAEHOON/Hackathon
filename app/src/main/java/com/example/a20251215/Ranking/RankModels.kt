package com.example.a20251215.Ranking

data class UserScore(
    val userId: Int,
    val name: String,
    val certCount: Int
)

data class RankItem(
    val badge: String, // 🥇/🥈/🥉 or 😡/😭/😮‍💨 or "4"
    val name: String,
    val count: String, // "인증 10회"
    val sub: String    // "이번 달 1등" / "이번 달 워스트 1"
)
