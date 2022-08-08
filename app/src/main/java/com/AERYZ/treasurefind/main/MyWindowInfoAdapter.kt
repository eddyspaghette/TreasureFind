package com.AERYZ.treasurefind.main

import android.R
import android.view.View
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker


class MyWindowInfoAdapter:GoogleMap.InfoWindowAdapter {
    override fun getInfoContents(p0: Marker): View? {
        // Getting view from the layout file info_window_layout
        // Getting view from the layout file info_window_layout
        val v: View = getLayoutInflater().inflate(R.layout.windowlayout, null)

        // Getting the position from the marker

        // Getting the position from the marker
        val latLng: LatLng = arg0.getPosition()

        // Getting reference to the TextView to set latitude

        // Getting reference to the TextView to set latitude
        val tvLat = v.findViewById<View>(R.id.tv_lat) as TextView

        // Getting reference to the TextView to set longitude

        // Getting reference to the TextView to set longitude
        val tvLng = v.findViewById<View>(R.id.tv_lng) as TextView

        // Setting the latitude

        // Setting the latitude
        tvLat.text = "Latitude:" + latLng.latitude

        // Setting the longitude

        // Setting the longitude
        tvLng.text = "Longitude:" + latLng.longitude

        // Returning the view containing InfoWindow contents

        // Returning the view containing InfoWindow contents
        return v
    }

    override fun getInfoWindow(p0: Marker): View? {
        TODO("Not yet implemented")
        return null
    }

}