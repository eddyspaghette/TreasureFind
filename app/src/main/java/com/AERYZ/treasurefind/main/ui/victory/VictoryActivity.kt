package com.AERYZ.treasurefind.main.ui.victory

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.AERYZ.treasurefind.R
import com.AERYZ.treasurefind.main.entry_point.MainActivity
import com.AERYZ.treasurefind.main.ui.hider_map.HiderMapActivity

class VictoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_victory)

        val wid = intent.getStringExtra(HiderMapActivity.wid_KEY)

        val wid_textview: TextView = findViewById(R.id.wid_textview)
        val btn_btm: Button = findViewById(R.id.btn_btm)
        wid_textview.text = wid
        btn_btm.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}