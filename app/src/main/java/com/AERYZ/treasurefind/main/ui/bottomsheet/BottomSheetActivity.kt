package com.AERYZ.treasurefind.main.ui.bottomsheet

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.AERYZ.treasurefind.R

class BottomSheetActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bottom_sheet)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

    }
}