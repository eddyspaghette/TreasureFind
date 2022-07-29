package com.AERYZ.treasurefind.main.ui.map

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.AERYZ.treasurefind.R
import com.AERYZ.treasurefind.databinding.ActivityMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener{

    private lateinit var mMap: GoogleMap
    private lateinit var marker: Marker
    private lateinit var tempmarker: Marker
    private lateinit var startmarker: Marker
    private lateinit var binding: ActivityMapsBinding
    private val PERMISSION_REQUEST_CODE = 0
    private var locationPermissionGranted = false
    private lateinit var locationManager: LocationManager
    private var mapCentered = false
    private lateinit var  markerOptions: MarkerOptions
    private lateinit var startmarkerOptions:MarkerOptions
    private lateinit var  polylineOptions: PolylineOptions
    private lateinit var  polylines: ArrayList<Polyline>

    //service
    private lateinit var service_intent: Intent
    private var isBind = false
    private lateinit var myViewModel:ServiceViewModel
    private lateinit var appContext: Context
    private val BIND_STATUS_KEY = "bind_status_key"
    //lastKnown location current location
    private var lastKnownLocation: Location? = null
    private lateinit var latLng:LatLng
    private lateinit var latarray:ArrayList<Float>
    private lateinit var lngarray:ArrayList<Float>
    private val calendar = Calendar.getInstance()
    private var duration:Int=0
    private var avg_speed:Int=0

    companion object {
        //        private val TAG = MapsActivityCurrentPlace::class.java.simpleName
        private const val DEFAULT_ZOOM = 15
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1

        // Keys for storing activity state.
        // [START maps_current_place_state_keys]
        private const val KEY_CAMERA_POSITION = "camera_position"
        private const val KEY_LOCATION = "location"
        // [END maps_current_place_state_keys]

        // Used for selecting the current place.
        private const val M_MAX_ENTRIES = 5
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        latarray= ArrayList()
        lngarray= ArrayList()
        appContext = this.applicationContext
        service_intent= Intent(this,TrackService::class.java)
        myViewModel= ViewModelProvider(this).get(ServiceViewModel::class.java)
        this.startService(service_intent)
        bindService()

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        checkPermission()
    }

//    override fun onDestroy() {
//        super.onDestroy()
//        if (locationManager != null)
//            locationManager.removeUpdates(this)
//    }

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

        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
//        mMap.setOnMapClickListener(this)
//        mMap.setOnMapLongClickListener(this)
        polylineOptions = PolylineOptions()
        polylineOptions.color(Color.BLACK)
        polylines = java.util.ArrayList()
        markerOptions = MarkerOptions()
        initLocationManager()
//        checkPermission()
        println("check ------------------------------")
    }
    fun initLocationManager() {
        try {
            locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
            val criteria = Criteria()
            criteria.accuracy = Criteria.ACCURACY_FINE
            val provider : String? = locationManager.getBestProvider(criteria, true)
            if(provider != null) {
                val location = locationManager.getLastKnownLocation(provider)
                lastKnownLocation=location

            }
            latarray.add(lastKnownLocation!!.latitude.toFloat())
            lngarray.add(lastKnownLocation!!.longitude.toFloat())
//            myViewModel.lat.observe(this,Observer{it->
////                println("fininally goes here it =: $it")
//                lastKnownLocation!!.latitude=it
////                println("fininally goes here it11111 =: $it")
//            })
//            myViewModel.lng.observe(this, Observer { it ->
//                lastKnownLocation!!.longitude = it
////                println("fininally goes here it222222 =: $it")
//                tempmarker.remove()
//                val latLng=LatLng(lastKnownLocation!!.latitude,lastKnownLocation!!.longitude)
//                val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17f)
//                mMap.animateCamera(cameraUpdate)
//                markerOptions.position(latLng)
//                marker= mMap.addMarker(markerOptions)
//                tempmarker=marker
//                println("marker addddddddddddddddddddddddddd")
//                polylineOptions.add(latLng)
//                polylines.add(mMap.addPolyline(polylineOptions))
//                println("polyline addddd")
//
//            })
            myViewModel.location.observe(this, androidx.lifecycle.Observer {
                val location=it
                latarray.add(location.latitude.toFloat())
                lngarray.add(location.longitude.toFloat())
                if(location!=null){
                    onLocationChanged(location)
                }
                tempmarker.remove()
                val latLng=LatLng(location.latitude,location.longitude)
                val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17f)
                mMap.animateCamera(cameraUpdate)
//                markerOptions.position(latLng)
//                marker= mMap.addMarker(markerOptions)
//                tempmarker=marker
//                println("marker addddddddddddddddddddddddddd")
//                polylineOptions.add(latLng)
//                polylines.add(mMap.addPolyline(polylineOptions))
//                println("polyline addddd")
//                if(location!=null){
//                    onLocationChanged(location)
//                }
            })
//            lastKnownLocation?.let { onLocationChanged(it) }
            println("go there-------")


        } catch (e: SecurityException) {
        }
    }

    override fun onLocationChanged(location: Location) {
        val lat = location.latitude
        val lng = location.longitude

        val latLng = LatLng(lat, lng)
        if (!mapCentered) {
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17f)
            mMap.animateCamera(cameraUpdate)
            markerOptions.position(latLng)
            tempmarker= mMap.addMarker(markerOptions)!!

            startmarkerOptions= MarkerOptions()
            startmarkerOptions.position(latLng)
//            startmarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
            startmarker= mMap.addMarker(startmarkerOptions)!!
            polylineOptions.add(latLng)
            mapCentered = true

        }
    }

//    override fun onMapClick(latLng: LatLng) {
//        for (i in polylines.indices) polylines[i].remove()
//        polylineOptions.points.clear()
//    }
//
//    override fun onMapLongClick(latLng: LatLng) {
//        markerOptions.position(latLng!!)
//        mMap.addMarker(markerOptions)
//        polylineOptions.add(latLng)
//        polylines.add(mMap.addPolyline(polylineOptions))
//    }

    fun checkPermission() {
        if (Build.VERSION.SDK_INT < 23) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_CODE)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), PERMISSION_REQUEST_CODE)
//        else
//            initLocationManager()

    }

    fun onCancelClicked(view: View){
//         unBindService()
        this.stopService(service_intent)
        finish()
    }

    fun bindService(){
        if(!isBind){
            this.bindService(service_intent,myViewModel, Context.BIND_AUTO_CREATE)
            isBind=true
            println("bind service!!!!")
        }
    }
    private fun unBindService(){
        if (isBind) {
            appContext.unbindService(myViewModel)
            isBind = false
            println("unbind service!!!!!")
        }
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(BIND_STATUS_KEY, isBind)
    }
    override fun onProviderDisabled(provider: String) {}
    override fun onProviderEnabled(provider: String) {}

}