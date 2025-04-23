package com.example.minstrm

import android.net.Uri

data class DeviceInfo(
    val produkt: String,
    val model: String,
    val effekt: String,
    val estimeretTid: String
)

fun parseLLMResponse(imageUri: Uri): DeviceInfo {
    // Simulate a response from LLM - replace with actual network/API logic
    return DeviceInfo(
        produkt = "Vaskemaskine",
        model = "Bosch WAU28T",
        effekt = "2000W",
        estimeretTid = "1.5 timer"
    )
}