package com.AERYZ.treasurefind.main.util

import android.Manifest
import android.app.Activity
import android.app.Service
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.PolyUtil
import org.json.JSONObject
import java.lang.Math.pow
import java.lang.Math.sqrt
import kotlin.math.pow

object Util {
    fun checkPermissions(activity: Activity?) {
        if (Build.VERSION.SDK_INT < 23) return
        if (ContextCompat.checkSelfPermission(activity!!.applicationContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(activity.applicationContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(activity.applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(activity.applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(activity, arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ), 0)
        }
    }

    fun getBitmap(context: Context, imgUri: Uri): Bitmap {
        val bitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(imgUri))
        val matrix = Matrix()
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    //https://stackoverflow.com/questions/9015372/how-to-rotate-a-bitmap-90-degrees
    fun rotateBitmap(bitmap: Bitmap, angle: Float): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    fun getCurrentLocation(activity: Activity): LatLng {
        var res = LatLng(0.0, 0.0)
        try {
            val locationManager = activity.getSystemService(Service.LOCATION_SERVICE) as LocationManager

            val criteria = Criteria()
            criteria.accuracy = Criteria.ACCURACY_FINE

            //find best provider
            val provider = locationManager.getBestProvider(criteria,true)
            if (provider != null) {
                val location = locationManager.getLastKnownLocation(provider)
                res = LatLng(location!!.latitude, location.longitude)
            }
        } catch (e: SecurityException) {}
        return res
    }


    //return distance in meters
    fun calculateDistance(a: LatLng, b: LatLng): Float {
        val aLocation = Location("GPS")
        aLocation.latitude = a.latitude
        aLocation.longitude = a.longitude
        val bLocation = Location("GPS")
        bLocation.latitude = b.latitude
        bLocation.longitude = b.longitude
        return aLocation.distanceTo(bLocation)
    }
    fun showRouteOnMap(googleMap: GoogleMap, latlngStart: LatLng, latlngEnd: LatLng, apikey:String, context: Context): Int {
        val latLngOrigin = latlngStart
        val latLngDestination = latlngEnd
        val mapkey=apikey
        var distance:Int=0
        googleMap!!.addMarker(MarkerOptions().position(latLngOrigin).title("Ayala"))
        googleMap!!.addMarker(MarkerOptions().position(latLngDestination).title("SM City"))
        googleMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngOrigin, 14.5f))
        val path: MutableList<List<LatLng>> = ArrayList()
        val urlDirections = "https://maps.googleapis.com/maps/api/directions/json?origin=$%7BlatLngOrigin.latitude%7D,$%7BlatLngOrigin.longitude%7D&destination=$%7BlatLngDestination.latitude%7D,$%7BlatLngDestination.longitude%7D&key=$%7Bmapkey%7D"
        try {
            val directionsRequest = object : StringRequest(Method.GET, urlDirections, Response.Listener<String> {
                    response ->
                val jsonResponse = JSONObject(response)
                // Get routes
                val routes = jsonResponse.getJSONArray("routes")
                val legs = routes.getJSONObject(0).getJSONArray("legs")
                val steps = legs.getJSONObject(0).getJSONArray("steps")
                distance = legs.getJSONObject(0).getJSONObject("distance").getInt("value")
                for (i in 0 until steps.length()) {
                    val points = steps.getJSONObject(i).getJSONObject("polyline").getString("points")
                    path.add(PolyUtil.decode(points))
                }
                for (i in 0 until path.size) {
                    googleMap!!.addPolyline(PolylineOptions().addAll(path[i]).color(Color.RED))
                }
            }, Response.ErrorListener {
                    _ ->
            }){}
            val requestQueue = Volley.newRequestQueue(context)
            requestQueue.add(directionsRequest)
        }catch (e:Exception){
            Log.e("Exception: %s",e.message.toString())
        }
        return distance
    }
}