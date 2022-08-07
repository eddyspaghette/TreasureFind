package com.AERYZ.treasurefind.main.services


import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import com.AERYZ.treasurefind.R
import com.AERYZ.treasurefind.main.ui.seeker_map.SeekerMapActivity

class TrackingService : Service(), LocationListener {
    private lateinit var notificationManager: NotificationManager
    private lateinit var locationManager: LocationManager
    private lateinit var myBinder: MyBinder

    private val NOTIFICATION_ID = 777
    private val CHANNEL_ID = "Notification Channel"
    private var msgHandler: Handler?=null

    companion object{
        val LOC_KEY="LOCATION"
        val MSG_LOC_ID = 999
    }


    override fun onCreate() {
        super.onCreate()
        println("debug: onCreate() called")

        myBinder=MyBinder()
        showNotification()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        println("debug: onStartComand() startID:$startId")
        initLocationManager()
        return START_STICKY
    }
    override fun onBind(intent: Intent): IBinder? {
        println("debug: onBind() called")
        return myBinder
    }
    inner class MyBinder:Binder(){
        fun setmsgHandler(inputHandler:Handler){
            msgHandler=inputHandler
        }
    }

    fun initLocationManager() {
        try {
            locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

            val criteria = Criteria()
            criteria.accuracy = Criteria.ACCURACY_FINE

            //find best provider
            val provider = locationManager.getBestProvider(criteria,true)
            if (provider != null) {
                val location = locationManager.getLastKnownLocation(provider)
                Log.d("Debug", "Provider: $provider")
                if (location!=null)
                {
                    //find the first location
                    onLocationChanged(location)
                }
                locationManager.requestLocationUpdates(provider, 10000, 0f, this)
            }
        } catch (e: SecurityException) {}
    }

    override fun onLocationChanged(location: Location) {
        Log.d("Debug", "onLocationChanged")
        if (msgHandler != null) {
            val bundle = Bundle()
            //passing the whole object to the message
            bundle.putParcelable(LOC_KEY, location)
            val msg = msgHandler!!.obtainMessage()
            msg.data = bundle
            msg.what = MSG_LOC_ID
            msgHandler!!.sendMessage(msg)
        }
    }


    private fun showNotification() {
        val intent = Intent(this, SeekerMapActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(
            this,
            CHANNEL_ID
        )
        notificationBuilder.setContentTitle("TreasureFind Navigator")
        notificationBuilder.setContentText("Tap to come back to the game")
        notificationBuilder.setContentIntent(pendingIntent)
        notificationBuilder.setSmallIcon(R.drawable.tf_logo)
        notificationBuilder.setAutoCancel(false)
        notificationBuilder.setOngoing(true)
        val notification = notificationBuilder.build()

        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                "TrackingNotificationChannel",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        msgHandler = null
        return true
    }

    private fun cleanupTasks(){
        notificationManager.cancel(NOTIFICATION_ID)
    }

    override fun onDestroy() {
        super.onDestroy()
        cleanupTasks()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        cleanupTasks()
        stopSelf()
    }
}