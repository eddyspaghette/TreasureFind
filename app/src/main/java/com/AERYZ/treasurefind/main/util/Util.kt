package com.AERYZ.treasurefind.main.util

import android.Manifest
import android.app.Activity
import android.app.Service
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.PolyUtil
import org.json.JSONObject

/* Utility functions exist here, such as permissions and other misc functions*/
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
                if (location != null) {
                    res = LatLng(location.latitude, location.longitude)
                }
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

    fun showRouteOnMap(googleMap: GoogleMap, oldPolyLineArray: ArrayList<Polyline>, latlngStart: LatLng, latlngEnd: LatLng, context: Context): ArrayList<Polyline> {

        val ai: ApplicationInfo = context.applicationContext.packageManager
            .getApplicationInfo(context.applicationContext.packageName, PackageManager.GET_META_DATA)
        val MapsAPI_Key = ai.metaData["com.google.android.geo.API_KEY"].toString()

        while (oldPolyLineArray.size > 0) {
            oldPolyLineArray[0].remove()
            oldPolyLineArray.removeFirst()
        }

        val latLngOrigin = latlngStart
        val latLngDestination = latlngEnd
        val path: MutableList<List<LatLng>> = ArrayList()
        val urlDirections = "https://maps.googleapis.com/maps/api/directions/json?origin=${latLngOrigin.latitude},${latLngOrigin.longitude}&destination=${latLngDestination.latitude},${latLngDestination.longitude}&mode=walking&key=${MapsAPI_Key}"
        try {
            val directionsRequest = object : StringRequest(Method.GET, urlDirections, Response.Listener<String> {
                    response ->
                val jsonResponse = JSONObject(response)
                // Get routes
                val routes = jsonResponse.getJSONArray("routes")
                if (routes.length() != 0) {
                    val legs = routes.getJSONObject(0).getJSONArray("legs")
                    val steps = legs.getJSONObject(0).getJSONArray("steps")
                    for (i in 0 until steps.length()) {
                        val points = steps.getJSONObject(i).getJSONObject("polyline").getString("points")
                        path.add(PolyUtil.decode(points))
                    }
                    for (i in 0 until path.size) {
                        oldPolyLineArray.add(googleMap.addPolyline(PolylineOptions().addAll(path[i]).color(0xFF19336D.toInt())))
                    }
                }
            }, Response.ErrorListener {
                    _ ->
            }){}
            val requestQueue = Volley.newRequestQueue(context)
            requestQueue.add(directionsRequest)
        }catch (e:Exception){
            Log.e("Exception: %s",e.message.toString())
        }
        return oldPolyLineArray
    }

    fun calculateDistance(latlngStart: LatLng, latlngEnd: LatLng, context: Context): Int {
        val ai: ApplicationInfo = context.applicationContext.packageManager
            .getApplicationInfo(context.applicationContext.packageName, PackageManager.GET_META_DATA)
        val MapsAPI_Key = ai.metaData["com.google.android.geo.API_KEY"].toString()

        val latLngOrigin = latlngStart
        val latLngDestination = latlngEnd
        var distance:Int=0
        val urlDirections = "https://maps.googleapis.com/maps/api/directions/json?origin=${latLngOrigin.latitude},${latLngOrigin.longitude}&destination=${latLngDestination.latitude},${latLngDestination.longitude}&mode=walking&key=${MapsAPI_Key}"
        try {
            val directionsRequest = object : StringRequest(Method.GET, urlDirections, Response.Listener<String> {
                    response ->
                val jsonResponse = JSONObject(response)
                // Get routes
                val routes = jsonResponse.getJSONArray("routes")
                val legs = routes.getJSONObject(0).getJSONArray("legs")
                distance = legs.getJSONObject(0).getJSONObject("distance").getInt("value")
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

    fun checkInsideRadius(center: LatLng, radius: Double, location: LatLng): Boolean {
        if (calculateDistance(center, location) <= radius) {
            return true
        }
        return false
    }

    fun convertDpToPixel(dp: Float, resources: Resources): Float {
        return 1f * dp * (resources.displayMetrics.densityDpi * 1f / DisplayMetrics.DENSITY_DEFAULT)
    }
}
