//https://hamzaasif-mobileml.medium.com/getting-frames-of-live-camera-footage-as-bitmaps-in-android-using-camera2-api-kotlin-40ba8d3afc76
package com.AERYZ.treasurefind.main.ui.livecamera

import android.app.Fragment
import android.content.Context
import android.graphics.Bitmap
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.media.Image
import android.media.ImageReader
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.Surface
import android.widget.Button
import android.widget.ImageView
import com.AERYZ.treasurefind.R
import com.AERYZ.treasurefind.main.ui.livecamera.CameraConnectionFragment
import com.AERYZ.treasurefind.main.util.Util
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LiveCameraActivity : AppCompatActivity(), ImageReader.OnImageAvailableListener {

    private lateinit var imageView: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_camera)
        setFragment()

        val btn_capture: Button = findViewById(R.id.btn_capture)
        imageView = findViewById(R.id.livecamimageview)
        btn_capture.setOnLongClickListener() {
            isCapture = 1
            true
        }

    }

    var previewHeight = 0;
    var previewWidth = 0
    var sensorOrientation = 0;
    var isCapture = 0
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
    private var isProcessingFrame = false
    private val yuvBytes = arrayOfNulls<ByteArray>(3)
    private var rgbBytes: IntArray? = null
    private var yRowStride = 0
    private var postInferenceCallback: Runnable? = null
    private var imageConverter: Runnable? = null
    private var rgbFrameBitmap: Bitmap? = null
    private lateinit var rd: ImageReader

    //TODO getting frames of live camera footage and passing them to model
    override fun onImageAvailable(reader: ImageReader) {
        if (isCapture == 1) {
            isCapture = 0
            cameraprocess(reader)
            CoroutineScope(Main).launch {
                if (rgbFrameBitmap != null) {
                    val bitmap = Util.rotateBitmap(rgbFrameBitmap!!, 90f)
                    Log.d("Debug", "${bitmap!!.width} ${bitmap!!.height}")
                    imageView.setImageBitmap(bitmap)
                }
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