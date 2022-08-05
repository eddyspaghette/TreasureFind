package com.AERYZ.treasurefind.main.ui.seeker_map

import android.app.Fragment
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.media.Image
import android.media.ImageReader
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.AERYZ.treasurefind.R
import com.AERYZ.treasurefind.databinding.ActivitySeekermapBinding
import com.AERYZ.treasurefind.db.MyFirebase
import com.AERYZ.treasurefind.db.SR
import com.AERYZ.treasurefind.main.services.TrackingService
import com.AERYZ.treasurefind.main.ui.livecamera.CameraConnectionFragment
import com.AERYZ.treasurefind.main.ui.livecamera.ImageUtils
import com.AERYZ.treasurefind.main.util.Util
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.auth.FirebaseAuth

class SeekerMapActivity : AppCompatActivity(), OnMapReadyCallback, ImageReader.OnImageAvailableListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivitySeekermapBinding

    //service
    private lateinit var serviceIntent: Intent
    private var isBind = false
    private lateinit var mapViewModel: SeekerMapViewModel
    private lateinit var mapViewModelFactory: SeekerMapViewModelFactory
    private val BINDING_STATUS_KEY = "BINDING_STATUS"
    private var isFirstTimeCenter = false
    private val myFirebase = MyFirebase()
    private var tid: String = ""


    companion object {
        var tid_KEY = "tid"
        var who_KEY = "who" //0 is hider, 1 is seeker
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeekermapBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //back button
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.seeker_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        if (savedInstanceState != null) {
            isBind = savedInstanceState.getBoolean(BINDING_STATUS_KEY, false)
        }


        val who = intent.getIntExtra(who_KEY, 0)
        tid  = intent.getStringExtra(tid_KEY)!!
        val tid_TextView: TextView = findViewById(R.id.Text_tid)
        val temp = "tid: ${tid}"
        tid_TextView.setText(temp)

        //Service View Model
        mapViewModelFactory = SeekerMapViewModelFactory(tid!!)
        mapViewModel = ViewModelProvider(this, mapViewModelFactory)[SeekerMapViewModel::class.java]


        serviceIntent = Intent(this, TrackingService::class.java)
        startService(serviceIntent)
        bindService()

        //Getting number of seekers
        val numSeekers_TextView: TextView = findViewById(R.id.Text_numPlayers)
        myFirebase.getTreasure(tid!!, mapViewModel.treasure)
        mapViewModel.treasure.observe(this) {
            val text = "Joined: ${it.seekers.size} Seekers"
            numSeekers_TextView.setText(text)
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

        //live camera
        setFragment()
        val btn_capture: Button = findViewById(R.id.btn_capture)
        btn_capture.setOnLongClickListener() {
            isCapture = 1
            true
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
        try {
            mMap.isMyLocationEnabled = true
            mMap.uiSettings.isMyLocationButtonEnabled = true
            mMap.uiSettings.isCompassEnabled = true

        }
        catch (e: SecurityException)  {
            Log.e("Exception: %s", e.message.toString());
        }


        mapViewModel.location.observe(this) {
            if (!isFirstTimeCenter) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it!!.latitude,it.longitude),17f))
                isFirstTimeCenter = true
            }
        }

        mapViewModel.isInteract.observe(this) {
            setMapInteraction(mMap, it)
        }

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

    override fun onDestroy() {
        super.onDestroy()
        unBindService()
        stopService(serviceIntent)
    }

    //live camera
    var previewHeight = 0;
    var previewWidth = 0
    var sensorOrientation = 0;
    var isCapture = 0

    //TODO getting frames of live camera footage and passing them to model
    private var isProcessingFrame = false
    private val yuvBytes = arrayOfNulls<ByteArray>(3)
    private var rgbBytes: IntArray? = null
    private var yRowStride = 0
    private var postInferenceCallback: Runnable? = null
    private var imageConverter: Runnable? = null
    private var rgbFrameBitmap: Bitmap? = null

    //TODO fragment which show llive footage from camera
    protected fun setFragment() {
        val manager =
            getSystemService(Context.CAMERA_SERVICE) as CameraManager
        var cameraId: String? = null
        try {
            cameraId = manager.cameraIdList[0]
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
        val fragment: Fragment
        val camera2Fragment = CameraConnectionFragment.newInstance(
            object :
                CameraConnectionFragment.ConnectionCallback {
                override fun onPreviewSizeChosen(size: Size?, rotation: Int) {
                    previewHeight = size!!.height
                    previewWidth = size.width
                    sensorOrientation = rotation - getScreenOrientation()
                }
            },
            this,
            R.layout.fragment_camera,
            Size(480, 480)
        )
        camera2Fragment.setCamera(cameraId)
        fragment = camera2Fragment
        fragmentManager.beginTransaction().replace(R.id.container, fragment).commit()
    }

    protected fun getScreenOrientation(): Int {
        return when (windowManager.defaultDisplay.rotation) {
            Surface.ROTATION_270 -> 270
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_90 -> 90
            else -> 0
        }
    }



    //TODO getting frames of live camera footage and passing them to model
    override fun onImageAvailable(reader: ImageReader) {
        if (isCapture == 1) {
            cameraprocess(reader)
            //action with output image here
            isCapture = 0
            if (rgbFrameBitmap != null) {
                val bitmap = Util.rotateBitmap(rgbFrameBitmap!!, 90f)
                val uid = FirebaseAuth.getInstance().uid
                val sR = SR(uid!!, bitmap!!)
                myFirebase.updateSR(tid, sR)
                Toast.makeText(this, "Uploaded!", Toast.LENGTH_SHORT).show()
            }
        }
        val temp = reader.acquireLatestImage()
        if (temp!= null)
        {
            temp.close()
        }
    }


    private fun cameraprocess(reader: ImageReader) {
        if (previewWidth != 0 && previewHeight != 0) {
            if (rgbBytes == null) {
                rgbBytes = IntArray(previewWidth * previewHeight)
            }
            try {
                val image = reader.acquireLatestImage() ?: return
                if (!isProcessingFrame) {
                    isProcessingFrame = true
                    val planes = image.planes
                    fillBytes(planes, yuvBytes)
                    yRowStride = planes[0].rowStride
                    val uvRowStride = planes[1].rowStride
                    val uvPixelStride = planes[1].pixelStride
                    imageConverter = Runnable {
                        ImageUtils.convertYUV420ToARGB8888(
                            yuvBytes[0]!!,
                            yuvBytes[1]!!,
                            yuvBytes[2]!!,
                            previewWidth,
                            previewHeight,
                            yRowStride,
                            uvRowStride,
                            uvPixelStride,
                            rgbBytes!!
                        )
                    }
                    postInferenceCallback = Runnable {
                        image.close()
                        isProcessingFrame = false
                    }
                    processImage()
                } else {
                    image.close()
                }
            } catch (e: Exception) {
                return
            }
        }
    }

    private fun processImage() {
        imageConverter!!.run()
        rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888)
        rgbFrameBitmap?.setPixels(rgbBytes, 0, previewWidth, 0, 0, previewWidth, previewHeight)
        postInferenceCallback!!.run()
    }

    protected fun fillBytes(
        planes: Array<Image.Plane>,
        yuvBytes: Array<ByteArray?>
    ) {
        // Because of the variable row stride it's not possible to know in
        // advance the actual necessary dimensions of the yuv planes.
        for (i in planes.indices) {
            val buffer = planes[i].buffer
            if (yuvBytes[i] == null) {
                yuvBytes[i] = ByteArray(buffer.capacity())
            }
            buffer[yuvBytes[i]]
        }
    }

}