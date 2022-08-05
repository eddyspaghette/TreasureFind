package com.AERYZ.treasurefind.main.ui.treasuredetails

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.AERYZ.treasurefind.R
import com.AERYZ.treasurefind.db.MyFirebase
import com.AERYZ.treasurefind.main.ui.feed.FeedFragment
import com.AERYZ.treasurefind.main.ui.seeker_map.SeekerMapActivity
import com.google.firebase.auth.FirebaseAuth

class TreasureDetailsActivity : AppCompatActivity() {

    private var myFirebase = MyFirebase()
    private lateinit var treasureDetailsViewModel: TreasureDetailsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_treasure_details)
        //back button
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        //

        treasureDetailsViewModel = ViewModelProvider(this)[TreasureDetailsViewModel::class.java]

        val TDtitle: TextView = findViewById(R.id.TDtitle)
        val TDdesc: TextView = findViewById(R.id.TDdesc)
        val TDdistance: TextView = findViewById(R.id.TDdistance)
        val TDImageView: ImageView = findViewById(R.id.TDImageView)
        val TDtid: TextView = findViewById(R.id.TDtid)
        val TDAccept_btn: Button = findViewById(R.id.TDAccept_btn)


        val arguments = intent
        val tid = arguments.getStringExtra(FeedFragment.tid_KEY)
        if (tid != null) {
            TDtid.text = tid
            myFirebase.getTreasure(tid!! ,treasureDetailsViewModel.treasure)
            myFirebase.getTreasureImage(this, tid!!, treasureDetailsViewModel.image)
        }

        treasureDetailsViewModel.treasure.observe(this) {
            TDtitle.text = treasureDetailsViewModel.treasure.value?.title
            TDdesc.text = treasureDetailsViewModel.treasure.value?.desc
        }

        treasureDetailsViewModel.image.observe(this) {
            TDImageView.setImageBitmap(treasureDetailsViewModel.image.value)
        }


        TDAccept_btn.setOnClickListener() {
            val intent = Intent(this, SeekerMapActivity::class.java)
            intent.putExtra(SeekerMapActivity.tid_KEY, tid)
            intent.putExtra(SeekerMapActivity.who_KEY, 1) // 0 is hider, 1 is seeker
            myFirebase.updateSeeker(tid!!, FirebaseAuth.getInstance().uid!!)
            startActivity(intent)
        }
    }
}