package com.AERYZ.treasurefind

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.AERYZ.treasurefind.main.ui.hider_map.HiderMapActivity

class VictoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_victory)

        val wid = intent.getStringExtra(HiderMapActivity.wid_KEY)

        val wid_textview: TextView = findViewById(R.id.wid_textview)

        wid_textview.text = wid
    }
}