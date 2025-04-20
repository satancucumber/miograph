package com.callibri.miograph.screens.info

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import com.callibri.miograph.callibri.CallibriController

class InfoViewModel : ViewModel() {
    var infoText = ObservableField(CallibriController.fullInfo())
}