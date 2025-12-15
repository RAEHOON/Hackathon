package com.example.a20251215.Ranking

enum class RankType { BEST, WORST }

/**
 * 규칙
 * - 전체 합이 0이면 "랭킹 없음" → emptyList 반환 (텍스트만 띄우기 용)
 * - 동점이면 같은 rank(1,1,2 / 2등 동점이면 2,2)
 * - BEST: 높은 인증수 먼저
 * - WORST: 낮은 인증수 먼저
 */
fun buildRankItems(raw: List<UserScore>, type: RankType): List<RankItem> {
    // ✅ 아무도 인증 안 했으면(전원 0) 랭킹 자체 없음
    val total = raw.sumOf { it.certCount }
    if (total <= 0) return emptyList()

    val sorted = when (type) {
        RankType.BEST ->
            raw.sortedWith(compareByDescending<UserScore> { it.certCount }.thenBy { it.name })
        RankType.WORST ->
            raw.sortedWith(compareBy<UserScore> { it.certCount }.thenBy { it.name })
    }

    var rank = 0
    var prevScore: Int? = null

    return sorted.map { u ->
        // ✅ Dense ranking (동점 처리)
        if (prevScore == null || u.certCount != prevScore) rank += 1
        prevScore = u.certCount

        val badge = when (type) {
            RankType.BEST -> when (rank) {
                1 -> "🥇"
                2 -> "🥈"
                3 -> "🥉"
                else -> rank.toString()
            }
            RankType.WORST -> when (rank) {
                1 -> "😡"
                2 -> "😭"
                3 -> "😮‍💨"
                else -> rank.toString()
            }
        }

        val sub = when (type) {
            RankType.BEST -> "이번 달 ${rank}등"
            RankType.WORST -> "이번 달 워스트 ${rank}"
        }

        RankItem(
            badge = badge,
            name = u.name,
            count = "인증 ${u.certCount}회",
            sub = sub
        )
    }
}
