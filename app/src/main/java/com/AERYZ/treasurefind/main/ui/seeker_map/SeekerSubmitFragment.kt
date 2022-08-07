package com.AERYZ.treasurefind.main.ui.seeker_map

import android.content.Context.CAMERA_SERVICE
import android.content.Context.WINDOW_SERVICE
import android.graphics.Bitmap
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.media.Image
import android.media.ImageReader
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.*
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.AERYZ.treasurefind.R
import com.AERYZ.treasurefind.db.MyFirebase
import com.AERYZ.treasurefind.db.SR
import com.AERYZ.treasurefind.main.ui.livecamera.CameraConnectionFragment
import com.AERYZ.treasurefind.main.ui.livecamera.ImageUtils
import com.AERYZ.treasurefind.main.util.Util
import com.AERYZ.treasurefind.main.util.Util.calculateDistance
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth


class SeekerSubmitFragment : Fragment(), ImageReader.OnImageAvailableListener {

    private var myFirebase = MyFirebase()
    private var tid = ""
    private lateinit var mapViewModel: SeekerMapViewModel
    private lateinit var mapViewModelFactory: SeekerMapViewModelFactory


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_seeker_submit, container, false)

        //getting tid argument from previous
        tid = arguments?.getString(SeekerMapActivity.tid_KEY).toString()
        Log.d("Debug Seeker Submit", tid)

        //init view model
        mapViewModelFactory = SeekerMapViewModelFactory(tid)
        mapViewModel = ViewModelProvider(this, mapViewModelFactory)[SeekerMapViewModel::class.java]

        //live camera
        setFragment()
        val btn_capture: Button = view.findViewById(R.id.btn_capture)
        btn_capture.setOnLongClickListener() {
            isCapture = 1
            true
        }

        return view
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
        val manager = requireActivity().getSystemService(CAMERA_SERVICE) as CameraManager
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
        fragment = camera2Fragment as Fragment
        requireActivity().supportFragmentManager.beginTransaction().replace(R.id.container, fragment).commit()
    }

    protected fun getScreenOrientation(): Int {
        val windowManager = requireActivity().getSystemService(WINDOW_SERVICE) as WindowManager
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
                val location = Util.getCurrentLocation(requireActivity())
                val treasureLocation = LatLng(mapViewModel.treasure.value!!.latitude!!, mapViewModel.treasure.value!!.longitude!!)
                val threshold = 5.0 //in meters, change this for how far to accept the submit
                if (calculateDistance(location, treasureLocation) <= threshold) {
                    val sR = SR(tid, uid!!, location.latitude, location.longitude, bitmap!!)
                    myFirebase.addSR(resources, sR)
                    Toast.makeText(activity, "Uploaded!", Toast.LENGTH_SHORT).show()
                }
                else {
                    //TODO: Dialog of failed submit (Too far from the treasure)
                    Toast.makeText(activity, "Too far from the treasure", Toast.LENGTH_SHORT).show()
                }
            }
        }
        val temp = reader.acquireLatestImage()
        temp?.close()
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