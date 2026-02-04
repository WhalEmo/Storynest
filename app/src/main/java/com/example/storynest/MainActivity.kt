package com.example.storynest

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.storynest.RegisterLogin.RegisterLoginFragmnet
import com.example.storynest.HomePage.HomePageFragment
import com.example.storynest.HomePage.BarFragmnets.AddPostFragmnet
import com.example.storynest.HomePage.BarFragmnets.SearchFragment
import com.example.storynest.HomePage.BarFragmnets.ProfileFragmnet

class MainActivity : AppCompatActivity() {
    private lateinit var btnHome: ImageView
    private lateinit var btnSearch: ImageView
    private lateinit var btnAddPost: ImageView
    private lateinit var btnProfile: ImageView
    private lateinit var bottomBar: LinearLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        bottomBar=findViewById(R.id.bottomBar)
        btnHome = findViewById(R.id.btnHome)
        btnSearch = findViewById(R.id.btnSearch)
        btnAddPost = findViewById(R.id.btnAddPost)
        btnProfile = findViewById(R.id.btnProfile)

        if (intent.getBooleanExtra("showLogin", false)) {
            println("showLogin")
            bottomBar.visibility = View.GONE
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, RegisterLoginFragmnet())
                .commitNow()
        } else if(intent.getBooleanExtra("login",false)){
            bottomBar.visibility = View.GONE
            println("login")
            val fragment = RegisterLoginFragmnet()
                val bundle = Bundle()
                bundle.putBoolean("login", true)
                fragment.arguments = bundle
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commitNow()

        }else if(intent.getBooleanExtra("register",false)){
            bottomBar.visibility = View.GONE
            println("register")
            val fragment = RegisterLoginFragmnet()
            val bundle = Bundle()
            bundle.putBoolean("register", true)
            fragment.arguments = bundle
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commitNow()
        }else if(intent.getBooleanExtra("forgotpassword",false)){
            bottomBar.visibility = View.GONE
            val fragment = RegisterLoginFragmnet()
            val bundle = Bundle()
            bundle.putBoolean("forgotpassword", true)

            val token = intent.getStringExtra("TOKEN_KEY") // Launchden gelen sifre tokeni
            bundle.putString("TOKEN_KEY", token)

            fragment.arguments = bundle
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commitNow()
        }else {
            bottomBar.visibility = View.VISIBLE

            if (savedInstanceState == null) {
                openFragment(HomePageFragment())
            }

            setupBottomBarClicks()
        }
    }
    private fun setupBottomBarClicks() {

        btnHome.setOnClickListener {
            openFragment(HomePageFragment())
        }

        btnSearch.setOnClickListener {
            openFragment(SearchFragment())
        }

        btnAddPost.setOnClickListener {
            openFragment(AddPostFragmnet())
        }

        btnProfile.setOnClickListener {
            openFragment(ProfileFragmnet())
        }
    }

    private fun openFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

}