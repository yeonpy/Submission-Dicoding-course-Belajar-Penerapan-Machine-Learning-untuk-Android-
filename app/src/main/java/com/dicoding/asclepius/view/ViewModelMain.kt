package com.dicoding.asclepius.view

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ViewModelMain: ViewModel() {
    private val _previewImage = MutableLiveData<Uri>()
    val previewImage: LiveData<Uri> get() = _previewImage

    //live data
    fun setPreviewImage(uri: Uri?) {
        //set value from live data
        _previewImage.value = uri
    }
}