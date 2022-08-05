package com.AERYZ.treasurefind.main.ui.seeker_map

import android.app.Fragment
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.media.Image
import android.media.ImageReader
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.AERYZ.treasurefind.R
import com.AERYZ.treasurefind.databinding.ActivitySeekermapBinding
import com.AERYZ.treasurefind.db.MyFirebase
import com.AERYZ.treasurefind.db.SR
import com.AERYZ.treasurefind.main.services.TrackingService
import com.AERYZ.treasurefind.main.ui.livecamera.CameraConnectionFragment
import com.AERYZ.treasurefind.main.ui.livecamera.ImageUtils
import com.AERYZ.treasurefind.main.util.Util
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.auth.FirebaseAuth

class SeekerMapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivitySeekermapBinding

    //service
    private lateinit var serviceIntent: Intent
    private var isBind = false
    private lateinit var mapViewModel: SeekerMapViewModel
    private lateinit var mapViewModelFactory: SeekerMapViewModelFactory
    private val BINDING_STATUS_KEY = "BINDING_STATUS"
    private var isFirstTimeCenter = false
    private val myFirebase = MyFirebase()
    private var tid: String = ""


    companion object {
        var tid_KEY = "tid"
        var who_KEY = "who" //0 is hider, 1 is seeker
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeekermapBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //back button
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.seeker_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        if (savedInstanceState != null) {
            isBind = savedInstanceState.getBoolean(BINDING_STATUS_KEY, false)
        }


        val who = intent.getIntExtra(who_KEY, 0)
        tid  = intent.getStringExtra(tid_KEY)!!
        val tid_TextView: TextView = findViewById(R.id.Text_tid)
        val temp = "tid: ${tid}"
        tid_TextView.setText(temp)

        //Service View Model
        mapViewModelFactory = SeekerMapViewModelFactory(tid!!)
        mapViewModel = ViewModelProvider(this, mapViewModelFactory)[SeekerMapViewModel::class.java]


        serviceIntent = Intent(this, TrackingService::class.java)
        startService(serviceIntent)
        bindService()

        //Getting number of seekers
        val numSeekers_TextView: TextView = findViewById(R.id.Text_numPlayers)
        myFirebase.getTreasure(tid!!, mapViewModel.treasure)
        mapViewModel.treasure.observe(this) {
            val text = "Joined: ${it.seekers.size} Seekers"
            numSeekers_TextView.setText(text)
        }

        //bottom sheet
        val bottomSheet: View = findViewById(R.id.seeker_bottom_sheet_view)
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
        try {
            mMap.isMyLocationEnabled = true
            mMap.uiSettings.isMyLocationButtonEnabled = true
            mMap.uiSettings.isCompassEnabled = true

        }
        catch (e: SecurityException)  {
            Log.e("Exception: %s", e.message.toString());
        }


        mapViewModel.location.observe(this) {
            if (!isFirstTimeCenter) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it!!.latitude,it.longitude),17f))
                isFirstTimeCenter = true
            }
        }

        mapViewModel.isInteract.observe(this) {
            setMapInteraction(mMap, it)
        }

    }

    fun bindService(){
        if(!isBind){
            applicationContext.bindService(serviceIntent, mapViewModel, Context.BIND_AUTO_CREATE)
            isBind = true
            println("bind service!")
        }
    }

    private fun unBindService(){
        if (isBind) {
            applicationContext.unbindService(mapViewModel)
            isBind = false
            println("unbind service!!!!!")
        }
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(BINDING_STATUS_KEY, isBind)
    }

    override fun onDestroy() {
        super.onDestroy()
        unBindService()
        stopService(serviceIntent)
    }



}