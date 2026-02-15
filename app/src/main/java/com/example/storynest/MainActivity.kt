package com.example.storynest

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.storynest.Profile.ProfileData
import com.example.storynest.Profile.ProfileFragment
import com.example.storynest.Profile.ProfileMode

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
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
            following = 89
        )
        navigateTo(
            ProfileFragment.newInstance(
                mode = ProfileMode.MY_PROFILE
            )
        )

    }

    fun navigateTo(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.enter_from_right,
                R.anim.exit_to_left,
                R.anim.enter_from_left,
                R.anim.exit_to_right
            )
            .replace(R.id.nav_host, fragment)
            .addToBackStack(null)
            .commit()
    }



}