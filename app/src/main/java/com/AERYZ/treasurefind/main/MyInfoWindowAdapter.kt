package com.AERYZ.treasurefind.main

import android.app.Activity
import android.graphics.Bitmap
import android.view.View
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import com.AERYZ.treasurefind.R
import com.AERYZ.treasurefind.db.MyFirebase
import com.AERYZ.treasurefind.db.MyUser
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.firebase.firestore.ktx.toObject
import com.mikhaellopez.circularimageview.CircularImageView


class MyInfoWindowAdapter(var activity: Activity,
                          var seekers: HashMap<String,MutableLiveData<MyUser>>,
                          var seekersImage: HashMap<String,MutableLiveData<Bitmap>>): GoogleMap.InfoWindowAdapter {

    override fun getInfoContents(p0: Marker): View? {
        // Getting view from the layout file info_window_layout
        val view: View = activity.layoutInflater.inflate(R.layout.seekerinfowindowlayout, null)

        val seekerName_TextView = view.findViewById<TextView>(R.id.InfoWindowAuthorName)
        val seekerImageView = view.findViewById<CircularImageView>(R.id.InfoWindowImage)

        if (p0.title != "Treasure") {
            val seekerID = p0.title

            seekerName_TextView.text = seekers[seekerID]?.value?.userName

            seekerImageView.setImageBitmap(seekersImage[seekerID]?.value)
        }
        else
            seekerName_TextView.text = "Treasure"

        return view
    }

    override fun getInfoWindow(p0: Marker): View? {
        //TODO("Not yet implemented")
        return null
    }


}