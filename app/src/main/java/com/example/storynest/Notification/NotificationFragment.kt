package com.example.storynest.Notification

import NotificationAdapter
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.storynest.R
import com.example.storynest.databinding.ProfileNotificationFragmentBinding

class NotificationFragment : Fragment() {
    private lateinit var viewModel: NotiViewModel
    private lateinit var adapter: NotificationAdapter
    private lateinit var swipeCallback: ItemTouchHelper.SimpleCallback


    private var _binding: ProfileNotificationFragmentBinding? = null
    private val binding get() = _binding!!

    val testItems = mutableListOf(
        FollowResponseDTO(
            id = 1L,
            requester = SimpleUserDTO(
                userId = 101L,
                username = "ahmet_y",
                profileURL = "https://i.pravatar.cc/150?img=3"
            ),
            requested = SimpleUserDTO(
                userId = 999L,
                username = "current_user",
                profileURL = null
            ),
            status = FollowRequestStatus.PENDING,
            date = "2025-12-16T14:30:00"
        ),
        FollowResponseDTO(
            id = 2L,
            requester = SimpleUserDTO(
                userId = 102L,
                username = "melis_k",
                profileURL = "https://i.pravatar.cc/150?img=5"
            ),
            requested = SimpleUserDTO(
                userId = 999L,
                username = "current_user",
                profileURL = null
            ),
            status = FollowRequestStatus.PENDING,
            date = "2025-12-16T13:10:00"
        ),
        FollowResponseDTO(
            id = 3L,
            requester = SimpleUserDTO(
                userId = 103L,
                username = "emre_dev",
                profileURL = "https://i.pravatar.cc/150?img=8"
            ),
            requested = SimpleUserDTO(
                userId = 999L,
                username = "current_user",
                profileURL = null
            ),
            status = FollowRequestStatus.PENDING,
            date = "2025-12-15T22:45:00"
        )
    )


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ProfileNotificationFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[NotiViewModel::class.java]
        adapter = NotificationAdapter(
            items = testItems,
            onAccept = { item ->
                // İsteği kabul et
                Toast.makeText(requireContext(), "${item.requester.username} kabul edildi", Toast.LENGTH_SHORT).show()
            },
            onReject = { item ->
                // İsteği reddet
                Toast.makeText(requireContext(), "${item.requester.username} reddedildi", Toast.LENGTH_SHORT).show()
            }
        )
        binding.recycler.layoutManager = LinearLayoutManager(requireContext())
        binding.recycler.adapter = adapter
        swipeCallback = createSwipeCallback(requireContext())

        ItemTouchHelper(swipeCallback).attachToRecyclerView(binding.recycler)

        /*viewModel.getMyFollowPending()
        viewModel.pending.observe(viewLifecycleOwner){ pending ->
        }

         */
    }
    private fun createSwipeCallback(context: Context): ItemTouchHelper.SimpleCallback {
        return object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            private val deleteIcon: Drawable? =
                ContextCompat.getDrawable(context, R.drawable.settings) // Çarpı ikonu
            private val archiveIcon: Drawable? =
                ContextCompat.getDrawable(context, R.drawable.book) // Tik ikonu

            private val backgroundRed = ColorDrawable(Color.parseColor("#FF5252"))
            private val backgroundGreen = ColorDrawable(Color.parseColor("#4CAF50"))

            // İkonun kenardan ne kadar içeride duracağı (padding)
            private val iconMargin = 60

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                try {
                    viewHolder.itemView.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    if (position != RecyclerView.NO_POSITION) {
                        if (direction == ItemTouchHelper.RIGHT) {
                            // SAĞA KAYDIRMA -> KABUL ET
                            // viewModel.acceptItem(item) gibi işlemler...
                        } else {
                            // SOLA KAYDIRMA -> REDDET / SİL
                            // viewModel.rejectItem(item) gibi işlemler...
                        }

                        // Adapter'dan silme işlemi (Eğer veritabanından siliyorsan burayı güncelle)
                        adapter.removeItem(position)
                    }
                } catch (e: Exception) {
                    adapter.notifyItemChanged(position) // Hata olursa öğeyi geri getir
                }
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                if (dX == 0f) {
                    super.onChildDraw(
                        c,
                        recyclerView,
                        viewHolder,
                        dX,
                        dY,
                        actionState,
                        isCurrentlyActive
                    )
                    return
                }

                if (dX > 0) {
                    println(dX)
                    backgroundGreen.setBounds(
                        itemView.left,
                        itemView.top,
                        itemView.left + dX.toInt(),
                        itemView.bottom
                    )
                    backgroundGreen.draw(c)

                } else {
                    println(dX)
                    backgroundRed.setBounds(
                        itemView.right + dX.toInt(),
                        itemView.top,
                        itemView.right,
                        itemView.bottom
                    )
                    backgroundRed.draw(c)
                }

                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }
        }
    }


}