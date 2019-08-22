package com.mind.filetracking.ui.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class PageViewModel : ViewModel() {

    private val fragmentTypeLiveData = MutableLiveData<String>()
    val text: LiveData<String> = Transformations.map(fragmentTypeLiveData) { it }

    fun setFragmentType(fragmentType: String) {
        fragmentTypeLiveData.value = fragmentType
    }
}