package com.example.storynest.HomePage.viewModelHpHelper

import com.example.storynest.Comments.viewModelChelper.CommentFormatter.formatDecimal
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object PostFormatter {
    fun formatPostDate(postDate: String?): String {

        val parser = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS")

        val postUtc = LocalDateTime.parse(postDate, parser)
            .atZone(ZoneOffset.UTC)

        val postTr = postUtc.withZoneSameInstant(ZoneId.of("Europe/Istanbul"))

        val nowTr = ZonedDateTime.now(ZoneId.of("Europe/Istanbul"))

        val days = ChronoUnit.DAYS.between(postTr, nowTr)
        val hours = ChronoUnit.HOURS.between(postTr, nowTr)
        val minutes = ChronoUnit.MINUTES.between(postTr, nowTr)

        return when {
            days >= 7 -> {
                val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                postTr.format(formatter)
            }
            days >= 1 -> "$days gün önce"
            hours >= 1 -> "$hours saat önce"
            minutes >= 1 -> "$minutes dakika önce"
            else -> "Şimdi"
        }
    }

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

}