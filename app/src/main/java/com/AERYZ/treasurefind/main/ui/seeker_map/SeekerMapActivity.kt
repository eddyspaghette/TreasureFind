package com.AERYZ.treasurefind.main.ui.seeker_map

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.AERYZ.treasurefind.R
import com.AERYZ.treasurefind.main.ui.victory.VictoryActivity
import com.AERYZ.treasurefind.databinding.ActivitySeekermapBinding
import com.AERYZ.treasurefind.db.MyFirebase
import com.AERYZ.treasurefind.db.MyUser
import com.AERYZ.treasurefind.main.services.TrackingService
import com.AERYZ.treasurefind.main.util.Util
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.toObject
import java.util.*
import kotlin.random.Random

class SeekerMapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivitySeekermapBinding
    private lateinit var submitFragment: SeekerSubmitFragment
    private lateinit var waitFragment: SeekerWaitFragment
    private var markerOptions = MarkerOptions()
    private var circleOptions = CircleOptions()
    private lateinit var locatetreasure_btn: ImageView
    private var polylineArray: ArrayList<Polyline> = ArrayList()

    //service
    private lateinit var serviceIntent: Intent
    private var isBind = false
    private lateinit var mapViewModel: SeekerMapViewModel
    private lateinit var mapViewModelFactory: SeekerMapViewModelFactory
    private val BINDING_STATUS_KEY = "BINDING_STATUS"
    private var isFirstTimeCenter = false
    private var isLocateTreasureFirstTime = false
    private val myFirebase = MyFirebase()
    private val uid = FirebaseAuth.getInstance().uid!!
    private var tid: String = ""

    companion object {
        var tid_KEY = "tid"
        var wid_KEY = "wid"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeekermapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        myFirebase.updateUser(uid, "status", 1)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.seeker_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        if (savedInstanceState != null) {
            isBind = savedInstanceState.getBoolean(BINDING_STATUS_KEY, false)
        }

        tid  = intent.getStringExtra(tid_KEY)!!
        val tid_TextView: TextView = findViewById(R.id.Text_tid)
        val temp = "tid: ${tid}"
        tid_TextView.text = temp

        locatetreasure_btn = findViewById(R.id.locatetreasure_btn)

        //Service View Model
        mapViewModelFactory = SeekerMapViewModelFactory(tid)
        mapViewModel = ViewModelProvider(this, mapViewModelFactory)[SeekerMapViewModel::class.java]


        serviceIntent = Intent(this, TrackingService::class.java)
        startService(serviceIntent)
        bindService()

        val bundle = Bundle()
        bundle.putString(tid_KEY, tid)
        submitFragment = SeekerSubmitFragment()
        submitFragment.arguments = bundle

        waitFragment = SeekerWaitFragment()
        waitFragment.arguments = bundle

        //Getting status of hider
        val hoststatus_TextView: TextView = findViewById(R.id.Text_hostOnline)
        mapViewModel.hiderStatus.observe(this) {
            if (it == 0) {
                hoststatus_TextView.text = "Host: Offline"
            } else {
                hoststatus_TextView.text = "Host: Online"
            }
        }

        //Getting number of seekers
        val numSeekers_TextView: TextView = findViewById(R.id.Text_numPlayers)

        mapViewModel.treasure.observe(this) {
            val text = "Joined: ${it.seekers.size} Seekers"
            numSeekers_TextView.text = text


            if (it != null) {
                //fragment replace
                if (it.sr.indexOf(uid) == -1) {
                    supportFragmentManager.beginTransaction().replace(R.id.seeker_map_fragmentcontainerview, submitFragment).commit()
                }
                else {
                    supportFragmentManager.beginTransaction().replace(R.id.seeker_map_fragmentcontainerview, waitFragment).commit()
                }

                //if winner is determined
                if (it.wid != "") {
                    myFirebase.updateUser(uid, "in_session", "")
                    val intent = Intent(this, VictoryActivity::class.java)
                    intent.putExtra(wid_KEY, it.wid)
                    intent.putExtra(tid_KEY, tid)
                    startActivity(intent)
                    finish()
                }

                //update seeker list
                for (seekerID in it.seekers) {
                    if (!mapViewModel.seekers.containsKey(seekerID) && seekerID!=uid) {
                        myFirebase.getUserDocument(seekerID).get()
                            .addOnCompleteListener { task ->
                                mapViewModel.seekers[seekerID] = MutableLiveData(task.result.toObject<MyUser>())
                                mapViewModel.seekers[seekerID]!!.observe(this) { myUser ->
                                    markerOptions.position(LatLng(myUser.latitude, myUser.longitude))
                                    if (mapViewModel.markers[seekerID] != null) {
                                        mapViewModel.markers[seekerID]!!.remove()
                                    }
                                    //change this line for issue #110
                                    mapViewModel.markers[seekerID] = mMap.addMarker(markerOptions)!!
                                    mapViewModel.markers[seekerID]!!.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker_seeker))
                                    mapViewModel.markers[seekerID]?.title =seekerID
                                }
                            }
                        Log.d("Debug Seeker location changed", seekerID)
                        mapViewModel.SeekerUpdateListener(seekerID)
                    }
                }
            }
        }

        //update location to database
        mapViewModel.location.observe(this) {
            if (it != null) {
                mapViewModel.updateSeekerLocation(LatLng(it.latitude, it.longitude))
            }
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
        mMap.setOnMarkerClickListener { marker ->
            Log.d("debug","gose listener")
            for (seekerID in mapViewModel.markers.keys){
                Log.d("debug","gose here")
                if (seekerID == marker.title){
                    if(marker.isInfoWindowShown){
                        marker.hideInfoWindow()
                    }
                    else{
                        marker.showInfoWindow()
                    }
                }
                else{
                    marker.hideInfoWindow()
                }
            }
            true
        }
        try {
            mMap.isMyLocationEnabled = true
            mMap.uiSettings.isMyLocationButtonEnabled = true
            mMap.uiSettings.isCompassEnabled = true

        }
        catch (e: SecurityException)  {
            Log.e("Exception: %s", e.message.toString());
        }


        mapViewModel.location.observe(this) {
            val currentLocation = LatLng(it!!.latitude,it.longitude)
            if (!isFirstTimeCenter) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation,18f))
                isFirstTimeCenter = true


            }

            if (!Util.checkInsideRadius(mapViewModel.treasureFakeLocation, 50.0, currentLocation))
            {
                polylineArray = Util.showRouteOnMap(mMap, polylineArray, currentLocation, mapViewModel.treasureFakeLocation, this)
            } else {

                while (polylineArray.size > 0 ) {
                    if (polylineArray.size == 1) {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mapViewModel.treasureFakeLocation,19f))
                    }
                    polylineArray[0].remove()
                    polylineArray.removeFirst()
                }
            }
        }

        mapViewModel.treasure.observe(this) {
            if (!isLocateTreasureFirstTime) {
                isLocateTreasureFirstTime = true

                val noise_lat = (Random.nextFloat()*2.0-1.0)*0.0002 //change this for more or less noise
                val noise_lng = (Random.nextFloat()*2.0-1.0)*0.0002 //change this for more or less noise

                mapViewModel.treasureFakeLocation = LatLng(it!!.latitude!! + noise_lat, it.longitude!! + noise_lng)

                //treasure approximate location
                circleOptions.center(mapViewModel.treasureFakeLocation)
                circleOptions.radius(50.0)
                circleOptions.fillColor(0x220000FF)
                circleOptions.strokeColor(0x330000FF)
                mMap.addCircle(circleOptions)
            }
        }

        mapViewModel.isInteract.observe(this) {
            setMapInteraction(mMap, it)
        }

        locatetreasure_btn.setOnClickListener() {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mapViewModel.treasureFakeLocation,18f))
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

    override fun onPause() {
        super.onPause()
        myFirebase.updateUser(uid, "status", 0)
    }

    override fun onDestroy() {
        super.onDestroy()
        unBindService()
        stopService(serviceIntent)
    }
    override fun onBackPressed() {
        return
    }


}