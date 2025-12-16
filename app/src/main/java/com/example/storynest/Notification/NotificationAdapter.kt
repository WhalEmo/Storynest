import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.storynest.Notification.FollowResponseDTO
import com.example.storynest.R
import com.google.android.material.imageview.ShapeableImageView
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class NotificationAdapter(
    private val items: MutableList<FollowResponseDTO>,
    private var onAccept: (FollowResponseDTO) -> Unit,
    private var onReject: (FollowResponseDTO) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    val adapter = this

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgProfile: ShapeableImageView = itemView.findViewById(R.id.imgProfile)
        val txtUsername: TextView = itemView.findViewById(R.id.txtUsername)
        val txtDate: TextView = itemView.findViewById(R.id.txtDate)
        val btnAccept: View = itemView.findViewById(R.id.btnAccept)
        val btnReject: View = itemView.findViewById(R.id.btnReject)

        fun animateSwipe(isRight: Boolean, item: FollowResponseDTO) {
            val screenWidth = itemView.resources.displayMetrics.widthPixels.toFloat()
            val targetX = if (isRight) screenWidth else -screenWidth
            itemView.animate()
                .translationX(targetX)
                .alpha(0f)
                .setDuration(300)
                .withEndAction {
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        if (isRight) {
                            adapter.removeItem(items.indexOf(item))
                        } else {
                            adapter.removeItem(items.indexOf(item))
                        }
                    }
                }
                .start()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.notification_item, parent, false)
        return ViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.txtUsername.text = item.requester.username
        holder.txtDate.text = formatDate(item.date)

        holder.imgProfile.load(item.requester.profileURL) {
            crossfade(true)
        }

        holder.itemView.translationX = 0f
        holder.itemView.alpha = 1f

        // KABUL BUTONU (SAĞA KAYDIRMA)
        holder.btnAccept.setOnClickListener {
            holder.animateSwipe(isRight = true, item = item)
        }

        // RED BUTONU (SOLA KAYDIRMA)
        holder.btnReject.setOnClickListener {
            holder.animateSwipe(isRight = false, item = item)
        }


    }

    override fun getItemCount(): Int = items.size

    fun removeItem(position: Int) {
        items.removeAt(position)
        notifyItemRemoved(position)
    }

    fun getItem(position: Int): FollowResponseDTO = items[position]

    @RequiresApi(Build.VERSION_CODES.O)
    private fun formatDate(date: String): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        val requestTime = LocalDateTime.parse(date, formatter)
        val now = LocalDateTime.now(ZoneId.systemDefault())

        val minutes = ChronoUnit.MINUTES.between(requestTime, now)
        val hours = ChronoUnit.HOURS.between(requestTime, now)
        val days = ChronoUnit.DAYS.between(requestTime, now)

        val timeText = when {
            minutes < 1 -> "Az önce"
            minutes < 60 -> "$minutes dakika önce"
            hours < 24 -> "$hours saat önce"
            days < 7 -> "$days gün önce"
            else -> requestTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        }

        return "$timeText • İstek gönderdi"
    }


}
