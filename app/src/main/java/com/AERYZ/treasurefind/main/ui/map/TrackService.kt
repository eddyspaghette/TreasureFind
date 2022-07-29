package com.AERYZ.treasurefind.main.ui.map


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
import androidx.core.app.NotificationCompat
import com.AERYZ.treasurefind.R
import com.google.android.gms.maps.model.LatLng
import java.util.*

class TrackService : Service(),LocationListener{
    private lateinit var notificationManager: NotificationManager
    private val NOTIFICATION_ID = 777
    private val CHANNEL_ID = "notification channel"
    private lateinit var myBinder: MyBinder
    private var counter=0
    private lateinit var timer:Timer
    private lateinit var myTimerTask: TimerTask
    //    private lateinit var mySecondTimerTask:TimerTask
    private var msgHandler: Handler?=null
    private var locationHandler:Handler?=null
    private var avgHandler:Handler?=null
    private var curHandler:Handler?=null
    private var speedHandler:Handler?=null
    private var distanceHandler:Handler?=null
    private lateinit var location:Location
    private lateinit var prelocation:Location
    private var saveLocation:Boolean=false
    var lat:Double= 0.0
    var lng:Double=0.0
    private var cur_distance =0f
    private var distance:Float=0f
    private var avg_speed:Float=0f

    //GPS map
    private lateinit var locationManager: LocationManager
    private var mapCentered = false
    private lateinit var startDate:Date
    companion object{
        val INT_KEY = "int key"
        val LAT_KEY="lat key"
        val LNG_KEY="lng key"
        val AVG_KEY="avg key"
        val CUR_KEY="cur_key"
        val DIS_KEY="dis_key"
        val LOC_KEY="loc_key"
        val MSG_INT_VALUE = 0
        val MSG_LATLNG_VALUE=1
        val MSG_AVG_VALUE=2
        val MSG_CUR_VALUE=3
        val MSG_DIS_VALUE=4
        val MSG_LOC_VALUE=5

    }


    override fun onCreate() {
        super.onCreate()
        startDate=Calendar.getInstance().time

        println("debug: onCreate() called")
        myBinder=MyBinder()
        showNotification()
        timer= Timer()
        myTimerTask =MyGPSTask()
//        mySecondTimerTask=gpsTask()
        timer.scheduleAtFixedRate(myTimerTask,0,1000L)
//        timer.scheduleAtFixedRate(mySecondTimerTask,0,3000L)
    }

    override fun onDestroy() {
        super.onDestroy()
        cleanupTasks()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        println("debug: onStartComand() startID:$startId")
        initLocationManager()
        return START_NOT_STICKY
    }
    override fun onBind(intent: Intent): IBinder? {
        println("debug: onBind() called")
        return myBinder
    }
    inner class MyBinder:Binder(){
        fun setmsgHandler(inputHandler:Handler){
            this@TrackService.msgHandler=inputHandler
            this@TrackService.locationHandler=inputHandler
            this@TrackService.avgHandler=inputHandler
            this@TrackService.curHandler=inputHandler
            this@TrackService.speedHandler=inputHandler
            this@TrackService.distanceHandler=inputHandler
        }

    }

    override fun onUnbind(intent: Intent?): Boolean {
        return true
    }
    inner class MyGPSTask: TimerTask(){
        override fun run(){
            counter++
            println("----------------->counter:----------"+counter.toString())
            if(msgHandler!=null){
                val bundle= Bundle()
                bundle.putInt(INT_KEY,counter)
                val message = msgHandler!!.obtainMessage()
                message.data=bundle
                message.what= MSG_INT_VALUE
                msgHandler!!.sendMessage(message)
            }
        }
    }
    fun initLocationManager() {
        try {
            locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
            val criteria = Criteria()
            criteria.accuracy = Criteria.ACCURACY_FINE
            val provider = locationManager.getBestProvider(criteria, true)
            val location = locationManager.getLastKnownLocation(provider!!)
            println("called initmanager------------------------------")
            if(location != null){
                if(!saveLocation){
                    println("once here 0.0----------------------")
                    prelocation=location!!
                    saveLocation=true
                    cur_distance=0f
                    distance=distance+cur_distance
                    println("prelocation:$prelocation")
                }
                onLocationChanged(location)

            }
            locationManager.requestLocationUpdates(provider, 0, 0f, this)
        } catch (e: SecurityException) {
            println(e)
        }
    }
    override fun onLocationChanged(location: Location) {
        if (locationHandler != null) {
            if(!saveLocation){
                println("im fking deaddddddddddddddddd")
                prelocation=location!!
                saveLocation=true
                cur_distance=0f
                distance=distance+cur_distance
            }
            else{
                println("into the else prelocation:$prelocation,cur location:$location")
                cur_distance = prelocation.distanceTo(location)
                println("Current location-------------------------:$cur_distance total distance:$distance")
                //update previours location and total distance
                prelocation=location!!
                distance=distance+cur_distance
            }
            val bundle = Bundle()
            bundle.putParcelable(LOC_KEY, location)

            val message = locationHandler!!.obtainMessage()
            message.data = bundle
            message.what = MSG_LOC_VALUE
            locationHandler!!.sendMessage(message)
            lat=location.latitude
            lng=location.longitude
            //avg speed bundle
            val avgSpeedbundle=Bundle()
            val dateNow=Calendar.getInstance().time
            val totaltime= (dateNow.time-startDate.time)/1000
            avg_speed=(distance/totaltime)*3.6.toFloat()
            println("DATA------------------------------------------------")
            println("distance:$distance counter:$counter avg_speed:$avg_speed")
            avgSpeedbundle.putFloat(AVG_KEY,avg_speed)
            val avgSpeed_1=avgHandler!!.obtainMessage()
            avgSpeed_1.data=avgSpeedbundle
            avgSpeed_1.what= MSG_AVG_VALUE
            avgHandler!!.sendMessage(avgSpeed_1)
            //cur speed bundle
            val curSpeedbundle=Bundle()
            cur_distance=location.speed*3.6.toFloat()
            curSpeedbundle.putFloat(CUR_KEY,cur_distance)
            val curSpeed_1=curHandler!!.obtainMessage()
            curSpeed_1.data=curSpeedbundle
            curSpeed_1.what= MSG_CUR_VALUE
            curHandler!!.sendMessage(curSpeed_1)
            //distance bundle
            val distancebundle=Bundle()
            distancebundle.putFloat(DIS_KEY,distance*0.001.toFloat())
            val distance_1=distanceHandler!!.obtainMessage()
            distance_1.data=distancebundle
            distance_1.what= MSG_DIS_VALUE
            distanceHandler!!.sendMessage(distance_1)


        }

    }


    private fun showNotification() {
        val intent = Intent(this, MapsActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(
            this,
            CHANNEL_ID
        ) //XD: see book p1019 why we do not use Notification.Builder
        notificationBuilder.setSmallIcon(R.drawable.ic_location)
        notificationBuilder.setContentTitle("Service has started")
        notificationBuilder.setContentText("Tap me to go back")
        notificationBuilder.setContentIntent(pendingIntent)
        val notification = notificationBuilder.build()
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                "channel name",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        println("debug: app removed from the application list")
        cleanupTasks()
        stopSelf()
    }

    private fun cleanupTasks(){
        notificationManager.cancel(NOTIFICATION_ID)
        if (timer != null)
            timer.cancel()
        counter = 0
    }
    override fun onProviderDisabled(provider: String) {}
    override fun onProviderEnabled(provider: String) {}


}