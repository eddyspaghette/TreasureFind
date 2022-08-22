package com.AERYZ.treasurefind.main.ui.seeker_map

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Bitmap
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
import com.AERYZ.treasurefind.main.MyInfoWindowAdapter
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
import java.lang.Exception
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.random.Random

/*
 * Seeker is redirected here once they accept a treasure session
 */
class SeekerMapActivity : AppCompatActivity(), OnMapReadyCallback, MyFirebase.Listener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivitySeekermapBinding
    private lateinit var submitFragment: SeekerSubmitFragment
    private lateinit var waitFragment: SeekerWaitFragment
    private var markerOptions = MarkerOptions()
    private var circleOptions = CircleOptions()
    private lateinit var locatetreasure_btn: ImageView
    private var polylineArray: ArrayList<Polyline> = ArrayList()
    private lateinit var myInfoWindowAdapter: MyInfoWindowAdapter

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

    //treasure bitmap
    private lateinit var imageMutableLiveData :MutableLiveData<Bitmap>
    private var isPreviewMode = false

    companion object {
        var tid_KEY = "tid"
        var wid_KEY = "wid"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeekermapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        myFirebase.updateUser(uid, "status", 1)
        imageMutableLiveData = MutableLiveData(BitmapFactory.decodeResource(resources, R.drawable.tf_logo))

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.seeker_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        if (savedInstanceState != null) {
            isBind = savedInstanceState.getBoolean(BINDING_STATUS_KEY, false)
        }

        tid  = intent.getStringExtra(tid_KEY)!!
        val tid_TextView: TextView = findViewById(R.id.Text_tid)
        tid_TextView.text = "TreasureID: ${tid}"

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

        //Preview button
        myFirebase.getTreasureImage(this, tid, imageMutableLiveData)

        val preview_btn: ImageView = findViewById(R.id.seeker_preview_btn)

        preview_btn.setOnClickListener {
            isPreviewMode = !isPreviewMode

            if (isPreviewMode) {
                preview_btn.setImageBitmap(imageMutableLiveData.value)
                preview_btn.layoutParams.height = Util.convertDpToPixel(300f, resources).toInt()
                preview_btn.layoutParams.width = Util.convertDpToPixel(300f, resources).toInt()
            } else {
                preview_btn.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.eye_outline))
                preview_btn.layoutParams.height = Util.convertDpToPixel(37f, resources).toInt()
                preview_btn.layoutParams.width = Util.convertDpToPixel(37f, resources).toInt()
            }

        }

        imageMutableLiveData.observe(this) {
            if (isPreviewMode) {
                preview_btn.setImageBitmap(it)
            }
        }


        //Getting status of hider
        val hoststatus_TextView: TextView = findViewById(R.id.Text_hostOnline)
        val hoststatus_ImageView: ImageView = findViewById(R.id.Image_hostOnline)
        mapViewModel.hiderStatus.observe(this) {
            if (it == 0) {
                hoststatus_TextView.text = "Offline"
                hoststatus_ImageView.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.offline_circle))
            } else {
                hoststatus_TextView.text = "Online"
                hoststatus_ImageView.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.online_circle))
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

                // if winner is the current user
                if (it.wid == uid) {

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
                                mapViewModel.seekersImage[seekerID] = MutableLiveData()
                                myFirebase.getProfileImage(this, seekerID, mapViewModel.seekersImage[seekerID]!!, this)
                            }
                        Log.d("Debug Seeker location changed", seekerID)
                        mapViewModel.SeekerUpdateListener(seekerID)
                    }
                }

                for (seekerID in mapViewModel.seekers.keys) {
                    if (!it.seekers.contains(seekerID)) {
                        mapViewModel.seekers.remove(seekerID)
                        mapViewModel.markers[seekerID]!!.remove()
                        mapViewModel.seekersImage.remove(seekerID)
                        mapViewModel.seekers_size.value = mapViewModel.seekers_size.value?.minus(1)
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
        //add quit button
        val quitButton = findViewById<Button>(R.id.btn_seeker_giveup)
        quitButton.setOnClickListener{
            myFirebase.removeSR(tid,uid)
            myFirebase.removeSeeker(tid,uid)
            myFirebase.updateUser(uid,"in_session","")
            finish()
        }

        mapViewModel.myUser.observe(this) {
            if (it != null && it.in_session == "") {
                finish()
            }
        }
    }

    private fun setMapInteraction(mMap: GoogleMap, value: Boolean) {
        mMap.uiSettings.isMyLocationButtonEnabled = value
        mMap.uiSettings.setAllGesturesEnabled(value)
        mMap.uiSettings.isCompassEnabled = value
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        myInfoWindowAdapter = MyInfoWindowAdapter(this, HashMap(), HashMap())
        mMap.setInfoWindowAdapter(myInfoWindowAdapter)


        mapViewModel.seekers_size.observe(this) {
            myInfoWindowAdapter.seekers = mapViewModel.seekers
            myInfoWindowAdapter.seekersImage = mapViewModel.seekersImage
            println("Debug I'm here")
        }

        mMap.setOnMarkerClickListener { marker ->
            if (marker.isInfoWindowShown) {
                marker.hideInfoWindow()
            }
            else {
                marker.showInfoWindow()
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

                //further loading animation edit here.#146



                ///////////////////////////////////////////
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

    override fun onSuccess() {
        mapViewModel.seekers_size.postValue(mapViewModel.seekers_size.value?.plus(1))
    }

    override fun onFailure(exception: Exception) {
        //TODO("Not yet implemented")
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