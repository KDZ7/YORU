package com.example.yoru

import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import org.opencv.android.CameraBridgeViewBase


class CameraBridge : AppCompatActivity() {
    private lateinit var closeBtn: Button
    private lateinit var cameraBridgeViewBase: CameraBridgeViewBase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camerabridge)

        cameraBridgeViewBase = findViewById(R.id.cameraView)
        requestPermissions()
        lifecycle.addObserver(Detector(this, cameraBridgeViewBase))

        closeBtn = findViewById(R.id.closeBtn_id)
        closeBtn.setOnClickListener {
            finish()
        }
    }


    private fun requestPermissions() {
        if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 0)
        else
            cameraBridgeViewBase.setCameraPermissionGranted()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            0 ->
                if (grantResults.isNotEmpty())
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                        cameraBridgeViewBase.setCameraPermissionGranted()
                    else
                        requestPermissions()
        }
    }
}
