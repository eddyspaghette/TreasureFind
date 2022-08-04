package com.AERYZ.treasurefind.main.ui.fragmentanimation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Button
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainer
import androidx.fragment.app.FragmentTransaction
import com.AERYZ.treasurefind.R

class FragmentAnimationActivity : AppCompatActivity() {
    private lateinit var fragmentContainer: FragmentContainer
    private lateinit var linearLayout: LinearLayout
    private lateinit var gestureDetector:GestureDetector
    private lateinit var frag_A: Fragment
    private lateinit var frag_B: Fragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fragment_animation)
        val btn_add: Button = findViewById(R.id.btn_add_frag)
        val btn_remove: Button = findViewById(R.id.btn_remove_frag)
        frag_A = AFragment()
        frag_B = BFragment()

        val transaction = supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(R.anim.fade_in, R.anim.exit_to_right, R.anim.fade_in, R.anim.exit_to_right)
        transaction.addToBackStack(null)
        transaction.add(R.id.fragment_container, frag_A).commit()
        btn_add.setOnClickListener() {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.setCustomAnimations(R.anim.fade_in, R.anim.exit_to_right, R.anim.fade_in, R.anim.exit_to_right)
            transaction.replace(R.id.fragment_container, frag_B).commit()
        }

        btn_remove.setOnClickListener() {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.setCustomAnimations(R.anim.fade_in, R.anim.exit_to_right, R.anim.fade_in, R.anim.exit_to_right)
            transaction.replace(R.id.fragment_container, frag_A).commit()
        }
    }

}