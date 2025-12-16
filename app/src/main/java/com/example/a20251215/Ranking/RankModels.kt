package com.example.a20251215.Ranking

// BEST / WORST 구분
enum class RankType { BEST, WORST }

// 서버에서 받아온 랭킹을 계산용으로 변환한 모델
data class UserScore(
    val userId: Int,
    val name: String,
    val certCount: Int
)

// RecyclerView 한 줄(UI) 모델
data class RankItem(
    val badge: String,  // 🥇 / 🥈 / 🥉 / 😡 / 😭 / 😮‍💨 / "4"
    val name: String,
    val count: String,  // "인증 10회"
    val sub: String     // "이번 달 1등" / "이번 달 워스트 1"
)
