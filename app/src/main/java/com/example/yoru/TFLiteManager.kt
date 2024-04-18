package com.example.yoru

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.GpuDelegate
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class TFLiteManager(
    private val context: Context,
    private val modelFile: String,
    private val targetFile: String,
    var inputPixels: Int = 200
) {
    private var interpreter: Interpreter? = null
    private var gpuDelegate: GpuDelegate? = null
    lateinit var targetList: ArrayList<String>

    init {
        initialize()
    }

    private fun initialize() {
        try {
            gpuDelegate = GpuDelegate()
            val options = Interpreter.Options().apply { addDelegate(gpuDelegate) }
            interpreter = Interpreter(loadModel(), options)
            targetList = ArrayList(loadTarget())
        } catch (e: Exception) {
            Log.e("TFLite", "Error initializing TensorFlow Lite Interpreter: ", e)
        }
    }

    private fun loadModel(): MappedByteBuffer {
        val assetFileDescriptor = context.assets.openFd(modelFile)
        FileInputStream(assetFileDescriptor.fileDescriptor).use { fileInputStream ->
            val channel = fileInputStream.channel
            val startOffset = assetFileDescriptor.startOffset
            val declaredLength = assetFileDescriptor.declaredLength
            return channel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength).also {
                channel.close()
                assetFileDescriptor.close()
            }
        }
    }

    private fun loadTarget(): List<String> {
        val tmpList = ArrayList<String>()
        context.assets.open(targetFile).bufferedReader().use { reader ->
            var line: String? = reader.readLine()
            while (line != null) {
                tmpList.add(line)
                line = reader.readLine()
            }
        }
        return tmpList
    }

    fun predict(input: Mat): Map<Int, Any> {
        var bitmap = Bitmap.createBitmap(input.cols(), input.rows(), Bitmap.Config.RGB_565)
        Utils.matToBitmap(input, bitmap)
        bitmap = Bitmap.createScaledBitmap(bitmap, inputPixels, inputPixels, false)
        val byteBuffer: ByteBuffer = convertBitmapToByteBuffer(bitmap)

        val outputMap = mutableMapOf<Int, Any>()

        val locations = Array(1) { Array(10) { FloatArray(4) } }
        val classes = Array(1) { FloatArray(10) }
        val scores = Array(1) { FloatArray(10) }
        val numberOfDetections = FloatArray(1)

        outputMap[0] = locations
        outputMap[1] = classes
        outputMap[2] = scores
        outputMap[3] = numberOfDetections

        interpreter?.runForMultipleInputsOutputs(arrayOf(byteBuffer), outputMap)

        return outputMap
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val bufferShapeCapacity = bitmap.width * bitmap.height * 3
        val byteBuffer =
            ByteBuffer.allocateDirect(bufferShapeCapacity).apply { order(ByteOrder.nativeOrder()) }
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        for (pixel in pixels) {
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF
            byteBuffer.put((r.toByte()))
            byteBuffer.put((g.toByte()))
            byteBuffer.put((b.toByte()))
        }
        byteBuffer.rewind()
        return byteBuffer
    }

    fun reinitialize() {
        release()
        initialize()
    }

    fun release() {
        interpreter?.close()
        gpuDelegate?.close()
        interpreter = null
        gpuDelegate = null
    }
}