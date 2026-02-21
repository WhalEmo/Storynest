package com.example.storynest

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.storynest.Profile.ProfileData
import com.example.storynest.Profile.ProfileFragment
import com.example.storynest.Profile.ProfileMode

class MainActivity : AppCompatActivity() {

    private val navigator = Navigator


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val userId = 8L
        val at = "https://images.unsplash.com/photo-1502685104226-ee32379fefbe?w=800"
        val profileData: ProfileData = ProfileData(
            id = 1,
            username = "emrullah.dev",
            email = "emrullah@example.com",
            name = "Emrullah",
            surname = "Uygun",
            profile = at,
            biography = "Android Developer • Coffee Lover ☕ • Kotlin ❤️",
            followers = 128,
            following = 89,
            isFollowing = false,
            isOwnProfile = true,
            isFollower = false
        )

        navigator.openProfile(
            activity = this as AppCompatActivity,
            id = userId,
            mode = ProfileMode.MY_PROFILE
        )

    }

    fun dumpStack() {
        supportFragmentManager.fragments.forEach {
            Log.d("STACK_DUMP",
                "Fragment=${it.tag} hash=${it.hashCode()} added=${it.isAdded} visible=${it.isVisible}"
            )
        }
    }


}