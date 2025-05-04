package com.example.minstrm.viewmodel

// Mathias HC

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.minstrm.model.Device

class DeviceViewModel : ViewModel() {
    private val _devices = mutableStateListOf<Device>()
    val devices: List<Device> get() = _devices

    fun addDevice(device: Device) {
        _devices.add(device)
    }

    fun updateDevice(index: Int, device: Device) {
        if (index in _devices.indices) _devices[index] = device
    }

    fun deleteDevice(index: Int) {
        if (index in _devices.indices) _devices.removeAt(index)
    }
}