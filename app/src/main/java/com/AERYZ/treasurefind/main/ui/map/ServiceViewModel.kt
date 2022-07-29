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
    private var myMessageHandler = MyMessageHandler(Looper.getMainLooper())
    private val _location=MutableLiveData<Location>()
    val location:LiveData<Location>
        get() {
            return _location
        }

    override fun onServiceConnected(name: ComponentName, iBinder: IBinder) {
        println("debug: ViewModel: onServiceConnected() called; ComponentName: $name")
        val tempBinder = iBinder as TrackingService.MyBinder
        tempBinder.setmsgHandler(myMessageHandler)
    }

    override fun onServiceDisconnected(name: ComponentName) {
        println("debug: Activity: onServiceDisconnected() called~~~")
    }

    inner class MyMessageHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {

            if(msg.what==TrackingService.MSG_LOC_ID){
                val bundle=msg.data
                _location.value=bundle.getParcelable(TrackingService.LOC_KEY)
            }
        }
    }
}