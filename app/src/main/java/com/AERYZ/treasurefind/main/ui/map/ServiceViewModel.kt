package com.AERYZ.treasurefind.main.ui.map

import android.content.ComponentName
import android.content.ServiceConnection
import android.location.Location
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ServiceViewModel:ViewModel(),ServiceConnection {
    private var myMessageHandler: MyMessageHandler
    private val _counter = MutableLiveData<Int>()
    private val _lat=MutableLiveData<Double>()
    private val _lng=MutableLiveData<Double>()
    private val _avgSpeed=MutableLiveData<Float>()
    private val _curSpeed=MutableLiveData<Float>()
    private val _distance=MutableLiveData<Float>()
    private val _location=MutableLiveData<Location>()
    val location:LiveData<Location>
        get() {
            return _location
        }
    val counter: LiveData<Int>
        get() {
            return _counter
        }
    val lat:LiveData<Double>
        get() {
            return _lat
        }
    val lng:LiveData<Double>
        get() {
            return _lng
        }
    val avgSpeed:LiveData<Float>
        get() {
            return _avgSpeed
        }
    val curSpeed:LiveData<Float>
        get() {
            return _curSpeed
        }
    val distance_1:LiveData<Float>
        get() {
            return _distance
        }

    init {
        myMessageHandler = MyMessageHandler(Looper.getMainLooper())
    }

    override fun onServiceConnected(name: ComponentName, iBinder: IBinder) {
        println("debug: ViewModel: onServiceConnected() called; ComponentName: $name")
        val tempBinder = iBinder as TrackService.MyBinder
        tempBinder.setmsgHandler(myMessageHandler)
    }

    override fun onServiceDisconnected(name: ComponentName) {
        println("debug: Activity: onServiceDisconnected() called~~~")
    }

    inner class MyMessageHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            if (msg.what == TrackService.MSG_INT_VALUE) {
                val bundle = msg.data
                _counter.value = bundle.getInt(TrackService.INT_KEY)
            }
            if (msg.what == TrackService.MSG_LATLNG_VALUE) {
                val bundle = msg.data
                _lat.value = bundle.getDouble(TrackService.LAT_KEY)
            }
            if (msg.what == TrackService.MSG_LATLNG_VALUE) {
                val bundle = msg.data
                _lng.value = bundle.getDouble(TrackService.LNG_KEY)
            }
            if(msg.what == TrackService.MSG_AVG_VALUE){
                val bundle =msg.data
                _avgSpeed.value=bundle.getFloat(TrackService.AVG_KEY)
            }
            if(msg.what==TrackService.MSG_CUR_VALUE){
                val bundle =msg.data
                _curSpeed.value=bundle.getFloat(TrackService.CUR_KEY)
            }
            if(msg.what==TrackService.MSG_LOC_VALUE){
                val bundle=msg.data
                _location.value=bundle.getParcelable(TrackService.LOC_KEY)
            }
            if(msg.what==TrackService.MSG_DIS_VALUE){
                val bundle=msg.data
                _distance.value=bundle.getFloat(TrackService.DIS_KEY)
            }

        }
    }
}