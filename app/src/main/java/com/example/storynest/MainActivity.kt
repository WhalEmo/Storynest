package com.example.storynest

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.storynest.Profile.MyProfile
import com.example.storynest.Profile.MyProfileFragment

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
        val myProfile: MyProfile = MyProfile(
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
        val myProfileFragment = MyProfileFragment.newInstance(myProfile)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer,myProfileFragment )
            .commit()
    }




}