package com.example.storynest.Notification

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.storynest.Notification.Adapter.NotificationRow
import retrofit2.Response
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationService @Inject constructor(
    private val notificationController: NotificationApiController
){

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getFollowRequests(): List<NotificationRow> {
        return list(notificationController.getFollowRequests())
    }

    suspend fun acceptFollow(id: Long): Response<FollowResponseDTO> {
        return notificationController.acceptFollow(id)
    }

    suspend fun rejectFollow(id: Long): Response<FollowResponseDTO>{
        return notificationController.rejectFollow(id)
    }




    @RequiresApi(Build.VERSION_CODES.O)
    private fun list(
        itemList: List<FollowResponseDTO>
    ): List<NotificationRow> {

        val notificationRows = mutableListOf<NotificationRow>()
        val now = LocalDateTime.now()
        val thirtyDaysAgo = now.minusDays(30)


        val sortedList = itemList.sortedByDescending {
            LocalDateTime.parse(it.date)
        }

        if (sortedList.isEmpty()) {
            notificationRows.add(
                NotificationRow.NotificationHeader("Bildirim yok")
            )
            return notificationRows
        }
        notificationRows.add(
            NotificationRow.NotificationHeader("Son 30 Gün")
        )

        for (index in sortedList.indices) {
            val item = sortedList[index]
            val itemDateTime = LocalDateTime.parse(item.date)

            if (itemDateTime.isBefore(thirtyDaysAgo)){
                var indexOlder = index
                notificationRows.add(
                    NotificationRow.NotificationHeader("Daha Eski")
                )
                while (indexOlder<sortedList.size){
                    notificationRows.add(
                        NotificationRow.NotificationItem(sortedList[indexOlder])
                    )
                    indexOlder++
                }
                break
            }
            notificationRows.add(
                NotificationRow.NotificationItem(item)
            )
        }

        return notificationRows
    }

}