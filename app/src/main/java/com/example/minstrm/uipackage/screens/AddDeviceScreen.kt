package com.example.minstrm.uipackage.screens

// Mathias HC

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.example.minstrm.aiAPI.DeviceInfo
import com.example.minstrm.aiAPI.parseLLMResponse
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


@Composable
fun AddDeviceScreen(
    initialDevice: DeviceInfo?,
    onDeviceSaved: (DeviceInfo) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // initialize from initialDevice if editing
    var produkt by remember { mutableStateOf(initialDevice?.produkt ?: "") }
    var model by remember { mutableStateOf(initialDevice?.model ?: "") }
    var effekt by remember { mutableStateOf(initialDevice?.effekt ?: "") }
    var estimeretTid by remember { mutableStateOf(initialDevice?.estimeretTid ?: "") }

    var isLoading by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    val imageBitmap = remember { mutableStateOf<Bitmap?>(null) }

    // permission for Android 13+
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

    // pick from gallery
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
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
                    produkt = "Fejl"; model = "-"; effekt = "-"; estimeretTid = "-"
                } finally {
                    isLoading = false
                }
            }
        }
    }

    // take full‐res photo
    val takePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
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
                        produkt = "Fejl"; model = "-"; effekt = "-"; estimeretTid = "-"
                    } finally {
                        isLoading = false
                    }
                }
            }
        }
    }


    fun createImageFile(): File {
        val ts = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${ts}_", ".jpg", storageDir)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(45.dp))
        // Card with pick / camera
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
                            createImageFile().also { file ->
                                tempPhotoUri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    file
                                )
                                takePhotoLauncher.launch(tempPhotoUri!!)
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

        Spacer(Modifier.height(24.dp))

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(8.dp))
        }

        // preview selected
        selectedImageUri?.let {
            Image(
                painter = rememberAsyncImagePainter(it),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(vertical = 8.dp)
            )
        }
        imageBitmap.value?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(vertical = 8.dp)
            )
        }

        // form fields
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

        Spacer(Modifier.height(16.dp))

        // Save / Add button
        Button(
            onClick = {
                if (produkt.isNotBlank() && model.isNotBlank() && effekt.isNotBlank() && estimeretTid.isNotBlank()) {
                    onDeviceSaved(DeviceInfo(produkt, model, effekt, estimeretTid))
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007BFF))
        ) {
            Text(if (initialDevice == null) "Tilføj enhed" else "Opdater enhed", color = Color.White)
        }
    }
}
