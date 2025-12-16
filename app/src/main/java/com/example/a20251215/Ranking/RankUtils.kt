package com.example.a20251215.Ranking


fun buildRankItems(raw: List<UserScore>, type: RankType): List<RankItem> {

    val filtered = when (type) {
        RankType.BEST -> raw.filter { it.certCount > 0 }
        RankType.WORST -> raw
    }

    if (filtered.isEmpty()) return emptyList()

    val sorted = when (type) {
        RankType.BEST -> filtered.sortedWith(
            compareByDescending<UserScore> { it.certCount }.thenBy { it.name }
        )
        RankType.WORST -> filtered.sortedWith(
            compareBy<UserScore> { it.certCount }.thenBy { it.name }
        )
    }

    var rank = 0
    var prevScore: Int? = null

    return sorted.map { u ->
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
