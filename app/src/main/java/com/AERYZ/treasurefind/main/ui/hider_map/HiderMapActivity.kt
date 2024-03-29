package com.AERYZ.treasurefind.main.ui.hider_map

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.AERYZ.treasurefind.R
import com.AERYZ.treasurefind.main.ui.victory.VictoryActivity
import com.AERYZ.treasurefind.databinding.ActivityHidermapBinding
import com.AERYZ.treasurefind.db.MyFirebase
import com.AERYZ.treasurefind.db.MyUser
import com.AERYZ.treasurefind.main.MyInfoWindowAdapter
import com.AERYZ.treasurefind.main.util.Util
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.toObject
import java.lang.Exception

/* Entry point for the Hider once he starts a session */
class HiderMapActivity : AppCompatActivity(), OnMapReadyCallback, MyFirebase.ImageGetListener  {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityHidermapBinding

    private lateinit var mapViewModel: HiderMapViewModel
    private lateinit var mapViewModelFactory: HiderMapViewModelFactory
    private var isFirstTimeCenter = false
    private val myFirebase = MyFirebase()
    private val uid = FirebaseAuth.getInstance().uid!!
    private var tid: String = ""
    private lateinit var myInfoWindowAdapter: MyInfoWindowAdapter

    private lateinit var hiderDoneFragment: HiderDoneFragment
    private lateinit var hiderValidateFragment: HiderValidateFragment
    private var markerOptions = MarkerOptions()
    private var circleOptions = CircleOptions()

    //treasure bitmap
    private lateinit var imageMutableLiveData :MutableLiveData<Bitmap>
    private var isPreviewMode = false

    companion object {
        var tid_KEY = "tid"
        var wid_KEY = "wid"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHidermapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tid  = intent.getStringExtra(tid_KEY)!!

        myFirebase.updateUser(uid, "status", 1)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.hider_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        imageMutableLiveData = MutableLiveData(BitmapFactory.decodeResource(resources, R.drawable.tf_logo))

        //Preview button
        myFirebase.getTreasureImage(this, tid, imageMutableLiveData)

        val preview_btn: ImageView = findViewById(R.id.hider_preview_btn)

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

        val tid_TextView: TextView = findViewById(R.id.Text_tid)
        val temp = "TreasureID: ${tid}"
        tid_TextView.setText(temp)

        //Service View Model
        mapViewModelFactory = HiderMapViewModelFactory(tid)
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


        val numSeekers_TextView: TextView = findViewById(R.id.Text_numPlayers)
        myFirebase.getTreasure(tid, mapViewModel.treasure)
        mapViewModel.treasure.observe(this) {

            if (it != null) {
                //Getting number of seekers
                val text = "Joined: ${it.seekers.size} Seekers"
                numSeekers_TextView.text = text

                //replace fragment
                if (it.sr.size == 0) {
                    supportFragmentManager.beginTransaction().replace(R.id.hider_map_fragmentcontainerview, hiderDoneFragment).commit()
                } else {
                    supportFragmentManager.beginTransaction().replace(R.id.hider_map_fragmentcontainerview, hiderValidateFragment).commit()
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
                    if (!mapViewModel.seekers.containsKey(seekerID)) {
                        myFirebase.getUserDocument(seekerID).get()
                            .addOnCompleteListener {
                                mapViewModel.seekers[seekerID] = MutableLiveData(it.result.toObject<MyUser>())
                                mapViewModel.seekers[seekerID]!!.observe(this) {
                                    markerOptions.position(LatLng(it.latitude, it.longitude))
                                    if (mapViewModel.markers[seekerID] != null) {
                                        mapViewModel.markers[seekerID]!!.remove()
                                    }
                                    //change this line for issue #110
                                    mapViewModel.markers[seekerID] = mMap.addMarker(markerOptions)!!
                                    mapViewModel.markers[seekerID]?.title = seekerID
                                    mapViewModel.markers[seekerID]!!.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker_seeker))
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

        //set quit button
        val quitButton= findViewById<Button>(R.id.btn_hider_giveup)
        quitButton.setOnClickListener{
            if(mapViewModel.treasure.value!=null){
                for (i in mapViewModel.treasure.value!!.seekers){
                    myFirebase.updateUser(i,"in_session","")
                    myFirebase.removeSR(tid,i)
                }
            }
            myFirebase.updateUser(uid,"in_session","")
            myFirebase.getTreasureDocument(tid).delete()
            finish()
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

        myInfoWindowAdapter = MyInfoWindowAdapter(this, HashMap(), HashMap())
        mMap.setInfoWindowAdapter(myInfoWindowAdapter)


        mapViewModel.seekers_size.observe(this) {
            myInfoWindowAdapter.seekers = mapViewModel.seekers
            myInfoWindowAdapter.seekersImage = mapViewModel.seekersImage
            println("Debug I'm here")
        }

        mMap.setOnMarkerClickListener { marker ->
            if (marker.title != "Treasure") {
                if (marker.isInfoWindowShown) {
                    marker.hideInfoWindow()
                }
                else {
                    marker.showInfoWindow()
                }
            }
            true
        }

        try {
            mMap.isMyLocationEnabled = true
            mMap.uiSettings.isMyLocationButtonEnabled = true
            mMap.uiSettings.isCompassEnabled = true
        } catch (e: SecurityException)  {
            Log.e("Exception: %s", e.message.toString());
        }

        mMap.setOnMyLocationButtonClickListener() {
            if (mapViewModel.treasure.value != null) {
                val treasure = mapViewModel.treasure.value!!
                Log.d("Debug", "on location click")
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(treasure.latitude!!,treasure.longitude!!),17f))
            }
            true
        }

        mapViewModel.isInteract.observe(this) {
            setMapInteraction(mMap, it)
        }

        mapViewModel.treasure.observe(this) {
            if (!isFirstTimeCenter) {
                isFirstTimeCenter = true
                val treasureLocation = LatLng(it.latitude!!, it.longitude!!)
                markerOptions.position(treasureLocation)

                //change this line to treasure icon issue #103
                val marker = mMap.addMarker(markerOptions)!!
                marker.title = "Treasure"
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker_treasuretwo))


                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude!!,it.longitude!!),17f))
                //further loading animation edit here.#146

                //////////////////////////////////////////

                circleOptions.center(treasureLocation)
                circleOptions.radius(50.0)
                circleOptions.fillColor(0x220000FF)
                circleOptions.strokeColor(0x330000FF)
                mMap.addCircle(circleOptions)
            }
        }
    }

    /* This function is used as a callback once getProfileImage returns */
    override fun onSuccess() {
        mapViewModel.seekers_size.postValue(mapViewModel.seekers_size.value?.plus(1))
    }

    /* Part of the listener interface callback as above */
    override fun onFailure(exception: Exception) {
        //TODO("Not yet implemented")
    }

    override fun onBackPressed() {
        return
    }

    override fun onPause() {
        super.onPause()
        myFirebase.updateUser(uid, "status", 0)
    }
}