package com.example.storynest

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.storynest.RegisterLogin.RegisterLoginFragmnet

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
        println("MainActivty")
        println("showLogin: "+intent.getBooleanExtra("showLogin", false))
        println("login: "+intent.getBooleanExtra("login", false))
        println("register: "+intent.getBooleanExtra("register", false))

        if (intent.getBooleanExtra("showLogin", false)) {
            println("showLogin")
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, RegisterLoginFragmnet())
                .commitNow()
        } else if(intent.getBooleanExtra("login",false)){
            println("login")
            val fragment = RegisterLoginFragmnet()
                val bundle = Bundle()
                bundle.putBoolean("login", true)
                fragment.arguments = bundle
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commitNow()

        }else if(intent.getBooleanExtra("register",false)){
            println("register")
            val fragment = RegisterLoginFragmnet()
            val bundle = Bundle()
            bundle.putBoolean("register", true)
            fragment.arguments = bundle
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commitNow()
        }else {
            //token gecerli
        }
    }
}