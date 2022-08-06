package com.AERYZ.treasurefind.main.ui.hider_map

import android.app.Fragment
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.media.Image
import android.media.ImageReader
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.AERYZ.treasurefind.R
import com.AERYZ.treasurefind.VictoryActivity
import com.AERYZ.treasurefind.databinding.ActivityHidermapBinding
import com.AERYZ.treasurefind.databinding.ActivitySeekermapBinding
import com.AERYZ.treasurefind.db.MyFirebase
import com.AERYZ.treasurefind.db.SR
import com.AERYZ.treasurefind.main.services.TrackingService
import com.AERYZ.treasurefind.main.ui.livecamera.CameraConnectionFragment
import com.AERYZ.treasurefind.main.ui.livecamera.ImageUtils
import com.AERYZ.treasurefind.main.ui.seeker_map.SeekerMapActivity
import com.AERYZ.treasurefind.main.ui.seeker_map.SeekerMapViewModel
import com.AERYZ.treasurefind.main.ui.seeker_map.SeekerMapViewModelFactory
import com.AERYZ.treasurefind.main.util.Util

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.auth.FirebaseAuth
import java.util.*

class HiderMapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityHidermapBinding

    private lateinit var mapViewModel: HiderMapViewModel
    private lateinit var mapViewModelFactory: HiderMapViewModelFactory
    private var isFirstTimeCenter = false
    private val myFirebase = MyFirebase()
    private var tid: String = ""

    private lateinit var hiderDoneFragment: HiderDoneFragment
    private lateinit var hiderValidateFragment: HiderValidateFragment

    companion object {
        var tid_KEY = "tid"
        var wid_KEY = "wid"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHidermapBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //back button
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.hider_map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        tid  = intent.getStringExtra(tid_KEY)!!
        val tid_TextView: TextView = findViewById(R.id.Text_tid)
        val temp = "tid: ${tid}"
        tid_TextView.setText(temp)

        //Service View Model
        mapViewModelFactory = HiderMapViewModelFactory(tid!!)
        mapViewModel = ViewModelProvider(this, mapViewModelFactory)[HiderMapViewModel::class.java]


        //bottom sheet
        val bottomSheet: View = findViewById(R.id.hider_bottom_sheet_view)
        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)

        //https://stackoverflow.com/questions/55485481/how-i-can-set-half-expanded-state-for-my-bottomsheet
        bottomSheetBehavior.addBottomSheetCallback(object: BottomSheetBehavior.BottomSheetCallback()
        {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (bottomSheetBehavior .state == BottomSheetBehavior.STATE_EXPANDED) {
                    mapViewModel.isInteract.value = false
                } else if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
                    mapViewModel.isInteract.value = true
                }
            }
            override fun onSlide(bottomSheet: View, slideOffset: Float) {

            }
        })

        val bundle = Bundle()
        bundle.putString(tid_KEY, tid)

        hiderDoneFragment = HiderDoneFragment()
        hiderDoneFragment.arguments = bundle
        hiderValidateFragment = HiderValidateFragment()
        hiderValidateFragment.arguments = bundle

        //Getting number of seekers
        val numSeekers_TextView: TextView = findViewById(R.id.Text_numPlayers)
        myFirebase.getTreasure(tid!!, mapViewModel.treasure)
        mapViewModel.treasure.observe(this) {
            val text = "Joined: ${it.seekers.size} Seekers"
            numSeekers_TextView.setText(text)

            if (it != null) {
                if (it.sr.size == 0) {
                    supportFragmentManager.beginTransaction().replace(R.id.hider_map_fragmentcontainerview, hiderDoneFragment).commit()
                } else {
                    supportFragmentManager.beginTransaction().replace(R.id.hider_map_fragmentcontainerview, hiderValidateFragment).commit()
                }

                //if winner is determined
                if (it.wid != "") {
                    val intent = Intent(this, VictoryActivity::class.java)
                    intent.putExtra(wid_KEY, it.wid)
                    startActivity(intent)
                }
            }
        }


    }

    private fun setMapInteraction(mMap: GoogleMap, value: Boolean) {
        mMap.uiSettings.isMyLocationButtonEnabled = value
        mMap.uiSettings.setAllGesturesEnabled(value)
        mMap.uiSettings.isCompassEnabled = value
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mapViewModel.isInteract.observe(this) {
            setMapInteraction(mMap, it)
        }

    }

}