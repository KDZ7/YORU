package com.example.yoru

import org.opencv.core.Mat

data class Tensor(var mRgba: Mat? = null, var mGray: Mat? = null) {
    init {
        mRgba = Mat()
        mGray = Mat()
    }
}
