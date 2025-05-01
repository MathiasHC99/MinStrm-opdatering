package com.example.minstrm

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asImageBitmap
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.util.Log
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
import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.platform.LocalContext


@Composable
fun AddDeviceScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, "Tilladelse til billeder kræves", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
        }
    }



    var produkt by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var effekt by remember { mutableStateOf("") }
    var estimeretTid by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val deviceList = remember { mutableStateListOf<DeviceInfo>() }
    val imageBitmap = remember { mutableStateOf<Bitmap?>(null) }
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }

    fun saveBitmapToFile(context: android.content.Context, bitmap: Bitmap): Uri? {
        return try {
            val fileName = "bitmap_${System.currentTimeMillis()}.jpg"
            val file = File(context.cacheDir, fileName)
            val outputStream = file.outputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            Log.e("BitmapSave", "Fejl ved gemning af bitmap", e)
            null
        }
    }

    val pickImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
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
                    Log.e("OpenAI_API_ERROR", "Fejl under OpenAI-kald", e)
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

    val takePhotoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            tempPhotoUri?.let { uri ->
                selectedImageUri = uri
                isLoading = true
                scope.launch {
                    try {
                        val info = parseLLMResponse(context, uri)
                        produkt = info.produkt
                        model = info.model
                        effekt = info.effekt
                        estimeretTid = info.estimeretTid
                    } catch (e: Exception) {
                        Log.e("OpenAI_API_ERROR", "Fejl under OpenAI-kald", e)
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
    }

    val previewLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            imageBitmap.value = bitmap
            val uri = saveBitmapToFile(context, bitmap)
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
                        Log.e("OpenAI_API_ERROR", "Fejl under OpenAI-kald", e)
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
    }


    fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
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
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { pickImageLauncher.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Vælg billede", color = Color(0xFF007BFF))
                        }
                        Button(
                            onClick = {
                                val photoFile = createImageFile()
                                tempPhotoUri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    photoFile
                                )
                                tempPhotoUri?.let { uri ->
                                    takePhotoLauncher.launch(uri)
                                }

                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Brug kamera", color = Color(0xFF007BFF))
                        }



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

            imageBitmap.value?.let { bitmap ->
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Kamerabillede",
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
                trailingIcon = {
                    if (produkt.isBlank()) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Mangler input",
                            tint = Color.Yellow
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = model,
                onValueChange = { model = it },
                label = { Text("Model") },
                trailingIcon = {
                    if (model.isBlank()) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Mangler input",
                            tint = Color.Yellow
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = effekt,
                onValueChange = { effekt = it },
                label = { Text("Effekt") },
                trailingIcon = {
                    if (effekt.isBlank()) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Mangler input",
                            tint = Color.Yellow
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = estimeretTid,
                onValueChange = { estimeretTid = it },
                label = { Text("Estimeret Tid") },
                trailingIcon = {
                    if (estimeretTid.isBlank()) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Mangler input",
                            tint = Color.Yellow
                        )
                    }
                },
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
