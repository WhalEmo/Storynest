package com.example.storynest.Notification.Adapter
import com.example.storynest.Notification.FollowResponseDTO

sealed class NotificationRow{
    data class NotificationHeader(val title: String) : NotificationRow()
    data class NotificationItem(
        val notification: FollowResponseDTO,
        val isUnread: Boolean = false,
        var isAccepted: Boolean = false,
        var isRejected: Boolean = false
    ) : NotificationRow()
}
