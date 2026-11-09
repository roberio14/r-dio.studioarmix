package com.example.player

data class SongVoteItem(
    val id: String,
    val title: String,
    val artist: String,
    val album: String? = null,
    val artworkUrl: String? = null,
    val fallbackResId: Int? = null,
    val likes: Int,
    val dislikes: Int,
    val userVote: String? = null, // "like", "dislike", null
    val isLive: Boolean = false,
    val programTitle: String = "Studio Ar Mix",
    val presenter: String = "Locutor de Plantão",
    val timeSlot: String = "Ao Vivo",
    val dayOfWeek: String = "Sexta-feira",
    val dayIndex: Int = 6, // 1 = Dom, 2 = Seg, ..., 6 = Sex (6º dia), 7 = Sáb
    val dateFormatted: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    val totalVotes: Int get() = likes + dislikes
    val approvalPercentage: Int
        get() = if (totalVotes > 0) ((likes.toFloat() / totalVotes.toFloat()) * 100).toInt() else 100
    val score: Int get() = likes - dislikes
}
