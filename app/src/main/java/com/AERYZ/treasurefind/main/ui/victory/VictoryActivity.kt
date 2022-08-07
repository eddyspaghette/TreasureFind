package com.AERYZ.treasurefind.main.ui.victory

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import com.AERYZ.treasurefind.R
import com.AERYZ.treasurefind.db.MyFirebase
import com.AERYZ.treasurefind.db.MyUser
import com.AERYZ.treasurefind.main.entry_point.MainActivity
import com.AERYZ.treasurefind.main.ui.hider_map.HiderMapActivity
import com.google.firebase.firestore.ktx.toObject

class VictoryActivity : AppCompatActivity() {
    private var myFirebase = MyFirebase()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_victory)

        val wid = intent.getStringExtra(HiderMapActivity.wid_KEY)!!
        val tid = intent.getStringExtra(HiderMapActivity.tid_KEY)!!
        val author_textview: TextView = findViewById(R.id.author_textview)
        val profileImageView: ImageView = findViewById(R.id.victory_avatar)
        val victory_imageview: ImageView = findViewById(R.id.victory_imageview)
        val btn_btm: Button = findViewById(R.id.btn_btm)

        val profileImageMutableLiveData = MutableLiveData(BitmapFactory.decodeResource(resources, R.drawable.tf_logo))
        val srImageMutableLiveData = MutableLiveData(BitmapFactory.decodeResource(resources, R.drawable.tf_logo))
        myFirebase.getProfileImage(this, wid, profileImageMutableLiveData)
        myFirebase.getSRImage(this, tid, wid, srImageMutableLiveData)

        profileImageMutableLiveData.observe(this) {
            profileImageView.setImageBitmap(it)
        }

        srImageMutableLiveData.observe(this) {
            victory_imageview.setImageBitmap(it)
        }

        myFirebase.getUserDocument(wid)
            .get()
            .addOnCompleteListener {
                val myUser = it.result.toObject<MyUser>()
                if (myUser != null) {
                    author_textview.text = myUser.userName
                }
            }



        btn_btm.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}