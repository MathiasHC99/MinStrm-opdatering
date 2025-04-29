package com.example.minstrm

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch

@Composable
fun AddDeviceScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var produkt by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var effekt by remember { mutableStateOf("") }
    var estimeretTid by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val deviceList = remember { mutableStateListOf<DeviceInfo>() }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            isLoading = true
            scope.launch {
                try {
                    val info = parseLLMResponse(context, it)
                    produkt = info.produkt
                    model = info.model
                    effekt = info.effekt
                    estimeretTid = info.estimeretTid
                } catch (e: Exception) {
                    produkt = "Fejl"
                    model = "-"
                    effekt = "-"
                    estimeretTid = "-"
                } finally {
                    isLoading = false
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(24.dp))

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF007BFF)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("✨ Smart tilføj", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text("Tilføj enheder ved blot at tage et billede", color = Color.White)
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { launcher.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Prøv det", color = Color(0xFF007BFF))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(8.dp))
            }

            selectedImageUri?.let { uri ->
                Image(
                    painter = rememberAsyncImagePainter(uri),
                    contentDescription = "Valgt billede",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(vertical = 8.dp)
                )
            }

            OutlinedTextField(
                value = produkt,
                onValueChange = { produkt = it },
                label = { Text("Produkt") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = model,
                onValueChange = { model = it },
                label = { Text("Model") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = effekt,
                onValueChange = { effekt = it },
                label = { Text("Effekt") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = estimeretTid,
                onValueChange = { estimeretTid = it },
                label = { Text("Estimeret Tid") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (produkt.isNotBlank() && model.isNotBlank() && effekt.isNotBlank() && estimeretTid.isNotBlank()) {
                        deviceList.add(
                            DeviceInfo(
                                produkt = produkt,
                                model = model,
                                effekt = effekt,
                                estimeretTid = estimeretTid
                            )
                        )
                        produkt = ""
                        model = ""
                        effekt = ""
                        estimeretTid = ""
                        selectedImageUri = null
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007BFF))
            ) {
                Text("Tilføj enhed", color = Color.White)
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (deviceList.isNotEmpty()) {
                Text(
                    text = "Tilføjede enheder:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }

        itemsIndexed(deviceList) { index, device ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE0E0E0)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${index + 1}. ${device.produkt} - ${device.model}", fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Effekt: ${device.effekt}")
                        Text("Programtid: ${device.estimeretTid}")
                    }
                    IconButton(onClick = {
                        if (index in deviceList.indices) {
                            deviceList.removeAt(index)
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Slet",
                            tint = Color.Red
                        )
                    }
                }
            }
        }
    }
}
