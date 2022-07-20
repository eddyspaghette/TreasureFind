package com.AERYZ.treasurefind

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.FragmentManager
import kotlin.math.log

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val loginFragment = LoginFragment()
        supportFragmentManager.beginTransaction().replace(R.id.frag_container,loginFragment, "login").commit()
    }
}