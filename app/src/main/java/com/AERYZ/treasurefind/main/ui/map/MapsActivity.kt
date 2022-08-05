package com.AERYZ.treasurefind.main.ui.map

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.AERYZ.treasurefind.R
import com.AERYZ.treasurefind.databinding.ActivityMapsBinding
import com.AERYZ.treasurefind.db.MyFirebase
import com.AERYZ.treasurefind.main.services.TrackingService
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.w3c.dom.Text

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var relativeLayout: RelativeLayout

    //service
    private lateinit var serviceIntent: Intent
    private var isBind = false
    private lateinit var mapsViewModel: MapsViewModel
    private lateinit var mapsViewModelFactory: MapsViewModelFactory
    private val BINDING_STATUS_KEY = "BINDING_STATUS"
    private var isFirstTimeCenter = false
    private val myFirebase = MyFirebase()



    companion object {
        var tid_KEY = "tid"
        var who_KEY = "who" //0 is hider, 1 is seeker
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        relativeLayout = findViewById(R.id.map_relative_layout)

        //back button
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        if (savedInstanceState != null) {
            isBind = savedInstanceState.getBoolean(BINDING_STATUS_KEY, false)
        }


        val who = intent.getIntExtra(who_KEY, 0)
        val tid  = intent.getStringExtra(tid_KEY)
        val tid_TextView: TextView = findViewById(R.id.Text_tid)
        val temp = "tid: ${tid}"
        tid_TextView.setText(temp)

        //Service View Model
        mapsViewModelFactory = MapsViewModelFactory(tid!!)
        mapsViewModel = ViewModelProvider(this, mapsViewModelFactory)[MapsViewModel::class.java]


        serviceIntent = Intent(this, TrackingService::class.java)
        startService(serviceIntent)
        bindService()

        //Getting number of seekers
        val numSeekers_TextView: TextView = findViewById(R.id.Text_numPlayers)
        myFirebase.getTreasure(tid!!, mapsViewModel.treasure)
        mapsViewModel.treasure.observe(this) {
            val text = "Joined: ${it.seekers.size} Seekers"
            numSeekers_TextView.setText(text)
        }

        //bottom sheet
        val bottomSheet: View = findViewById(R.id.bottom_sheet_view)
        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)

        //https://stackoverflow.com/questions/55485481/how-i-can-set-half-expanded-state-for-my-bottomsheet
        bottomSheetBehavior.addBottomSheetCallback(object: BottomSheetBehavior.BottomSheetCallback()
        {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (bottomSheetBehavior .state == BottomSheetBehavior.STATE_EXPANDED) {
                    mapsViewModel.isInteract.value = false
                } else if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
                    mapsViewModel.isInteract.value = true
                }
            }
            override fun onSlide(bottomSheet: View, slideOffset: Float) {

            }

        })


    }

    private fun setMapInteraction(mMap: GoogleMap, value: Boolean) {
        mMap.uiSettings.isMyLocationButtonEnabled = value
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


        mapsViewModel.location.observe(this) {
            if (!isFirstTimeCenter) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it!!.latitude,it.longitude),17f))
                isFirstTimeCenter = true
            }
        }

        mapsViewModel.isInteract.observe(this) {
            setMapInteraction(mMap, it)
        }

    }

    fun bindService(){
        if(!isBind){
            applicationContext.bindService(serviceIntent, mapsViewModel, Context.BIND_AUTO_CREATE)
            isBind = true
            println("bind service!")
        }
    }

    private fun unBindService(){
        if (isBind) {
            applicationContext.unbindService(mapsViewModel)
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