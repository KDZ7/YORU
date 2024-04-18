package com.example.yoru

import android.content.Context
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.OpenCVLoader
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import java.io.IOException

class Detector(
    context: Context,
    private val cameraBridgeViewBase: CameraBridgeViewBase,
    val threshold: Double = 0.6,
    val maxPixel: Int = 300
) : CameraBridgeViewBase.CvCameraViewListener2, DefaultLifecycleObserver {

    private lateinit var tfLiteManager: TFLiteManager
    private var tensor: Tensor? = null

    init {
        if (OpenCVLoader.initLocal()) {
            Log.d("OPENCV", "Loaded success")
            try {
                tfLiteManager =
                    TFLiteManager(
                        context,
                        "ssd_mobilenet_v1.tflite",
                        "ssd_mobilenet_v1.txt",
                        maxPixel
                    )
                cameraBridgeViewBase.setCvCameraViewListener(this)
                Log.d("TensorFlow Lite", "Success: Load model!")
            } catch (e: IOException) {
                Log.d("TensorFlow Lite", "Error: Failed to load model!")
            }
        } else {
            Log.d("OPENCV", "Loaded failed")
        }
    }

    override fun onCameraViewStarted(width: Int, height: Int) {
        tensor = Tensor()
        tensor?.mRgba?.create(height, width, CvType.CV_8UC4)
        tensor?.mGray?.create(height, width, CvType.CV_8UC1)
        cameraBridgeViewBase.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_BACK)
    }

    override fun onCameraViewStopped() {
        tensor?.mRgba?.release()
        tensor?.mGray?.release()
        tensor = null
    }

//    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame?): Mat {
//        tensor?.mRgba = inputFrame!!.rgba()
//
////        Imgproc.GaussianBlur(tensor?.mRgba, tensor?.mRgba, Size(5.0, 5.0), 0.0)
//        Imgproc.cvtColor(tensor?.mRgba, tensor?.mGray, Imgproc.COLOR_BGR2GRAY)
//        Imgproc.Canny(tensor?.mGray, tensor?.mGray, 100.0, 200.0)
//        val contours = ArrayList<MatOfPoint>()
//        val hierarchy = Mat()
//        Imgproc.findContours(
//            tensor?.mGray,
//            contours,
//            hierarchy,
//            Imgproc.RETR_TREE,
//            Imgproc.CHAIN_APPROX_SIMPLE
//        )
//        hierarchy.release()
//        for (contour in contours) {
//            val approxCurve = MatOfPoint2f()
//            val contour2f = MatOfPoint2f(*contour.toArray())
//            val epsilon = Imgproc.arcLength(contour2f, true) * 0.01
//            Imgproc.approxPolyDP(contour2f, approxCurve, epsilon, true)
//            val point = MatOfPoint(*approxCurve.toArray())
//            val rect = Imgproc.boundingRect(point)
//
//            if (rect.width > 300 && rect.height > 300) {
//                val roi = tensor?.mRgba!!.submat(rect) // Region of Interest
//                val outputMap = tfLiteManager.predict(roi)
//                val locations = outputMap[0] as Array<Array<FloatArray>>
//                val classes = outputMap[1] as Array<FloatArray>
//                val scores = outputMap[2] as Array<FloatArray>
//                val numberOfDetections = (outputMap[3] as FloatArray)[0]
//                for (i in 0 until numberOfDetections.toInt()) {
//                    val score = scores[0][i]
//                    if (score > 0.7) {
//                        val label = tfLiteManager.targetList[classes[0][i].toInt()]
//                        Imgproc.putText(
//                            tensor?.mRgba,
//                            "$label: ${(score * 100).toInt()}%",
//                            rect.tl(),
//                            Imgproc.FONT_HERSHEY_SIMPLEX,
//                            1.0,
//                            Scalar(0.0, 255.0, 255.0),
//                            2,
//                            Imgproc.LINE_AA
//                        )
//                        Imgproc.rectangle(
//                            tensor?.mRgba,
//                            rect.tl(),
//                            rect.br(),
//                            Scalar(0.0, 255.0, 0.0),
//                            2
//                        )
//                    }
//                }
//            }
//        }
//        return tensor?.mRgba!!
//    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame?): Mat {
        tfLiteManager.inputPixels = maxPixel
        tensor?.mRgba = inputFrame!!.rgba()
        val outputMap = tfLiteManager.predict(tensor?.mRgba!!)
        val locations = outputMap[0] as Array<Array<FloatArray>>
        val classes = outputMap[1] as Array<FloatArray>
        val scores = outputMap[2] as Array<FloatArray>
        val numberOfDetections = (outputMap[3] as FloatArray)[0]

        for (i in 0 until numberOfDetections.toInt()) {
            val score = scores[0][i]
            if (score > threshold) {
                val yMin = locations[0][i][0] * tensor?.mRgba!!.height()
                val xMin = locations[0][i][1] * tensor?.mRgba!!.width()
                val yMax = locations[0][i][2] * tensor?.mRgba!!.height()
                val xMax = locations[0][i][3] * tensor?.mRgba!!.width()
                val label = tfLiteManager.targetList[classes[0][i].toInt()]

                Imgproc.rectangle(
                    tensor?.mRgba,
                    Point(xMin.toDouble(), yMin.toDouble()),
                    Point(xMax.toDouble(), yMax.toDouble()),
                    Scalar(0.0, 255.0, 0.0),
                    2
                )
                Imgproc.putText(
                    tensor?.mRgba,
                    "$label: ${(score * 100).toInt()}%",
                    Point(xMin.toDouble() + 50, yMin.toDouble() + 50),
                    Imgproc.FONT_HERSHEY_SIMPLEX,
                    1.0,
                    Scalar(0.0, 255.0, 255.0),
                    2,
                    Imgproc.LINE_AA
                )
            }
        }
        return tensor?.mRgba!!
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        tfLiteManager.reinitialize()
        cameraBridgeViewBase.enableView()
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        cameraBridgeViewBase.disableView()
        tfLiteManager.release()
    }
}