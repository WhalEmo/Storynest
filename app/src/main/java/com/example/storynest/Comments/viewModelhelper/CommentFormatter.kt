package com.example.storynest.Comments.viewModelhelper

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object CommentFormatter {

     fun formatLike(likeCount: Int): String {
        return when {
            likeCount < 9_999 -> {
                likeCount.toString()
            }

            likeCount < 1_000_000 -> {
                val value = likeCount / 1_000.0
                formatDecimal(value) + "Bin"
            }

            else -> {
                val value = likeCount / 1_000_000.0
                formatDecimal(value) + "M"
            }
        }
    }
     fun formatDecimal(value: Double): String {
        val formatted = String.format("%.1f", value)
        return formatted.replace(".0", "").replace(".", ",")
    }


    fun formatCommentDate(postDate: String?): String {

        val postUtc = LocalDateTime.parse(
            postDate,
            DateTimeFormatter.ISO_LOCAL_DATE_TIME
        ).atZone(ZoneOffset.UTC)

        val postTr = postUtc.withZoneSameInstant(ZoneId.of("Europe/Istanbul"))
        val nowTr = ZonedDateTime.now(ZoneId.of("Europe/Istanbul"))

        val days = ChronoUnit.DAYS.between(postTr, nowTr)
        val hours = ChronoUnit.HOURS.between(postTr, nowTr)
        val minutes = ChronoUnit.MINUTES.between(postTr, nowTr)

        return when {
            days >= 7 -> postTr.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            days >= 1 -> "$days gün"
            hours >= 1 -> "$hours saat"
            minutes >= 1 -> "$minutes dakika"
            else -> "Şimdi"
        }
    }
}