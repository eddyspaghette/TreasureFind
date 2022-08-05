package com.AERYZ.treasurefind.main.ui.map

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.AERYZ.treasurefind.BuildConfig.MAPS_API_KEY
import com.AERYZ.treasurefind.R
import com.AERYZ.treasurefind.databinding.ActivityMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.libraries.places.api.Places
import com.google.firebase.firestore.Query
import com.google.gson.Gson
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private var originLatitude: Double = 28.5021359
    private var originLongitude: Double = 77.4054901
    private var destinationLatitude: Double = 28.5151087
    private var destinationLongitude: Double = 77.3932163

    //service
    private lateinit var serviceIntent: Intent
    private var isBind = false
    private lateinit var serviceViewModel: ServiceViewModel
    private val BINDING_STATUS_KEY = "BINDING_STATUS"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)



        /*
        // temp test locations: from Vancouver Art Gallery (49.2830° N, 123.1205° W) to Rennie Museum (49.2807° N, 123.1031° W)
        val fromLocation = LatLng(49.283, 123.121)
        val toLocation = LatLng(49.281, 123.103)
        calculateDirections(fromLocation, toLocation)
        */
        calculateDirections()

        if (savedInstanceState != null) {
            isBind = savedInstanceState.getBoolean(BINDING_STATUS_KEY, false)
        }

        //Service View Model
        serviceViewModel = ViewModelProvider(this)[ServiceViewModel::class.java]

        serviceIntent = Intent(this, TrackingService::class.java)
        startService(serviceIntent)
        bindService()
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

        } catch (e: SecurityException)  {
            Log.e("Exception: %s", e.message.toString());
        }

        val originLocation = LatLng(originLatitude, originLongitude)
        mMap.clear()
        mMap.addMarker(MarkerOptions().position(originLocation))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(originLocation, 18F))

        serviceViewModel.location.observe(this) {
            if (it != null)
            {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude,it.longitude),17f))
            }
        }
    }

    fun bindService(){
        if(!isBind){
            applicationContext.bindService(serviceIntent, serviceViewModel, Context.BIND_AUTO_CREATE)
            isBind = true
            println("bind service!")
        }
    }

    private fun unBindService(){
        if (isBind) {
            applicationContext.unbindService(serviceViewModel)
            isBind = false
            println("unbind service!!!!!")
        }
    }

//    private fun calculateDirections(fromlocation: LatLng, tolocation: LatLng){
    private fun calculateDirections(){
        val ai: ApplicationInfo = applicationContext.packageManager
            .getApplicationInfo(applicationContext.packageName, PackageManager.GET_META_DATA)
        val value = ai.metaData["com.google.android.geo.API_KEY"]
        val apiKey = value.toString()

        // Initializing the Places API with the help of our API_KEY
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, apiKey)
        }
        val gd = findViewById<Button>(R.id.directions)
        gd.setOnClickListener{
            val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync {
                mMap = it
                val originLocation = LatLng(originLatitude, originLongitude)
                mMap.addMarker(MarkerOptions().position(originLocation))
                val destinationLocation = LatLng(destinationLatitude, destinationLongitude)
                mMap.addMarker(MarkerOptions().position(destinationLocation))
                val urll = getDirectionURL(originLocation, destinationLocation, apiKey)
                GetDirection(urll).execute()
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(originLocation, 14F))
            }
        }
    }

    private fun getDirectionURL(origin:LatLng, dest:LatLng, secret: String) : String{
        return "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}" +
                "&destination=${dest.latitude},${dest.longitude}" +
                "&sensor=false" +
                "&mode=driving" +
                "&key=$secret"
    }

    inner class MapData {
        var routes = ArrayList<Routes>()
    }

    class Routes {
        var legs = ArrayList<Legs>()
    }

    class Legs {
        var distance = Distance()
        var duration = Duration()
        var end_address = ""
        var start_address = ""
        var end_location =Location()
        var start_location = Location()
        var steps = ArrayList<Steps>()
    }

    class Steps {
        var distance = Distance()
        var duration = Duration()
        var end_address = ""
        var start_address = ""
        var end_location =Location()
        var start_location = Location()
        var polyline = PolyLine()
        var travel_mode = ""
        var maneuver = ""
    }

    class Duration {
        var text = ""
        var value = 0
    }

    class Distance {
        var text = ""
        var value = 0
    }

    class PolyLine {
        var points = ""
    }

    class Location{
        var lat =""
        var lng =""
    }

    @SuppressLint("StaticFieldLeak")
    private inner class GetDirection(val url : String) : AsyncTask<Void, Void, List<List<LatLng>>>(){
        override fun doInBackground(vararg params: Void?): List<List<LatLng>> {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val data = response.body().toString()

            val result =  ArrayList<List<LatLng>>()
            try{
                val respObj = Gson().fromJson(data, MapData::class.java)
                val path =  ArrayList<LatLng>()
                for (i in 0 until respObj.routes[0].legs[0].steps.size){
                    path.addAll(decodePolyline(respObj.routes[0].legs[0].steps[i].polyline.points))
                }
                result.add(path)
            }catch (e:Exception){
                e.printStackTrace()
            }
            return result
        }

        override fun onPostExecute(result: List<List<LatLng>>) {
            val lineoption = PolylineOptions()
            for (i in result.indices){
                lineoption.addAll(result[i])
                lineoption.width(10f)
                lineoption.color(Color.GREEN)
                lineoption.geodesic(true)
            }
            mMap.addPolyline(lineoption)
        }

        fun decodePolyline(encoded: String): List<LatLng> {
            val poly = ArrayList<LatLng>()
            var index = 0
            val len = encoded.length
            var lat = 0
            var lng = 0
            while (index < len) {
                var b: Int
                var shift = 0
                var result = 0
                do {
                    b = encoded[index++].code - 63
                    result = result or (b and 0x1f shl shift)
                    shift += 5
                } while (b >= 0x20)
                val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
                lat += dlat
                shift = 0
                result = 0
                do {
                    b = encoded[index++].code - 63
                    result = result or (b and 0x1f shl shift)
                    shift += 5
                } while (b >= 0x20)
                val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
                lng += dlng
                val latLng = LatLng((lat.toDouble() / 1E5),(lng.toDouble() / 1E5))
                poly.add(latLng)
            }
            return poly
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(BINDING_STATUS_KEY, isBind)
    }

    override fun onDestroy() {
        super.onDestroy()
        unBindService()
    }

}