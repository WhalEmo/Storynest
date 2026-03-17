package com.example.storynest.Notification.Adapter

import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.storynest.R
import com.google.android.material.imageview.ShapeableImageView
import java.time.Duration
import java.time.LocalDateTime

class NotificationViewHolder(
    itemView: View,
    private val onAccept: (NotificationRow.NotificationItem) -> Unit,
    private val onReject: (NotificationRow.NotificationItem) -> Unit
): RecyclerView.ViewHolder(itemView) {

    private val actionContainer: View =
        itemView.findViewById(R.id.actionContainer)

    private val imgProfile: ShapeableImageView =
        itemView.findViewById(R.id.imgProfile)

    private val text: TextView =
        itemView.findViewById(R.id.explain)

    private val acceptButton: View =
        itemView.findViewById(R.id.btnAccept)

    private val rejectButton: View =
        itemView.findViewById(R.id.btnReject)

    private val btnMessage: View =
        itemView.findViewById(R.id.sendMessage)

    private val rejected: View =
        itemView.findViewById(R.id.rejected)



    @RequiresApi(Build.VERSION_CODES.O)
    fun bind(item: NotificationRow.NotificationItem) {
        val resource = item.notification
        text.text = buildFollowRequestText(
            resource.requester.username,
            timeShort = getTimeShort(resource.date)
        )


        imgProfile.load(resource.requester.profileURL) {
            crossfade(true)
        }

        val targetAlpha = if (item.isUnread) 1f else 0.6f

        itemView.animate()
            .alpha(targetAlpha)
            .setDuration(250)
            .setInterpolator(android.view.animation.DecelerateInterpolator())
            .start()

        if (item.isAccepted) {
            showMessageButton(animated = true)
        }
        else if (item.isRejected) {
            showRejectedButton(animated = true)
        }
        else {
            showActionButtons(animated = false)
        }

        acceptButton.setOnClickListener {
            showMessageButton(animated = true)
            onAccept(item)
        }
        rejectButton.setOnClickListener {
            showRejectedButton(animated = true)
            onReject(item)
        }
    }

    private fun showActionButtons(animated: Boolean) {
        actionContainer.visibility = View.VISIBLE
        actionContainer.alpha = 1f
        btnMessage.visibility = View.GONE
    }


    private fun showMessageButton(animated: Boolean) {

        if (!animated) {
            actionContainer.visibility = View.GONE
            btnMessage.visibility = View.VISIBLE
            btnMessage.alpha = 1f
            btnMessage.scaleX = 1f
            btnMessage.scaleY = 1f
            return
        }

        actionContainer.animate()
            .alpha(0f)
            .setDuration(150)
            .withEndAction {
                actionContainer.visibility = View.GONE
                btnMessage.visibility = View.VISIBLE

                btnMessage.scaleX = 0.8f
                btnMessage.scaleY = 0.8f
                btnMessage.alpha = 0f

                btnMessage.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(180)
                    .start()
            }
            .start()
    }
    private fun showRejectedButton(animated: Boolean) {
        if (!animated) {
            actionContainer.visibility = View.GONE
            rejected.visibility = View.VISIBLE
            rejected.alpha = 1f
            rejected.scaleX = 1f
            rejected.scaleY = 1f
            return
        }
        actionContainer.animate()
            .alpha(0f)
            .setDuration(150)
            .withEndAction {
                actionContainer.visibility = View.GONE
                rejected.visibility = View.VISIBLE
                rejected.scaleX = 0.8f
                rejected.scaleY = 0.8f
                rejected.alpha = 0f
                rejected.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(180)
                    .start()
            }
    }


    private fun buildFollowRequestText(
        username: String,
        timeShort: String
    ): SpannableString {

        val fullText = "$username seni takip etmek istiyor. $timeShort"
        val spannable = SpannableString(fullText)

        // 🔹 Username kalın
        spannable.setSpan(
            StyleSpan(Typeface.BOLD),
            0,
            username.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // 🔹 Zaman kısmını silik yap
        val timeStart = fullText.indexOf(timeShort)
        spannable.setSpan(
            ForegroundColorSpan(Color.parseColor("#8A8A8A")),
            timeStart,
            fullText.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // 🔹 Zaman biraz daha küçük olsun (isteğe bağlı)
        spannable.setSpan(
            RelativeSizeSpan(0.9f),
            timeStart,
            fullText.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        return spannable
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getTimeShort(date: String): String{
        val dateTime = LocalDateTime.parse(date)
        val now = LocalDateTime.now()
        val duration = Duration.between(dateTime, now)

        val seconds = duration.seconds
        val minutes = duration.toMinutes()
        val hours = duration.toHours()
        val days = duration.toDays()
        val weeks = days / 7
        val months = days / 30

        return when{
            seconds < 60 -> "$seconds"+"saniye"
            minutes < 60 -> "$minutes"+"dk"
            hours < 24 -> "$hours"+"s"
            days < 7 -> "$days"+"g"
            weeks < 4 -> "$weeks"+"h"
            months < 12 -> "$months"+"a"
            else -> "${months/12}"+"y"

        }
    }




}
