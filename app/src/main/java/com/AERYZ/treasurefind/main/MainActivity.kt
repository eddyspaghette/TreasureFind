package com.AERYZ.treasurefind.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.AERYZ.treasurefind.ui.login.LoginFragment
import com.AERYZ.treasurefind.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val loginFragment = LoginFragment()
        supportFragmentManager.beginTransaction().replace(R.id.frag_container,loginFragment, "login").commit()
    }
}