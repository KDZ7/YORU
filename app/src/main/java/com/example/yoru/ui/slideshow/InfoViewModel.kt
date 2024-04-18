package com.example.yoru.ui.slideshow

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class InfoViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "GitHUB: https://github.com/KDZ7/YORU" +
                "\n" +
                "Version: 0.0.1" +
                "\n" +
                "Prérequis : Caméra, TensorFlow"
    }

    val text: LiveData<String> = _text

}