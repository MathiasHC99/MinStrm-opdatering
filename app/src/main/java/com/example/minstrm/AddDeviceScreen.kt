package com.example.minstrm

import com.example.minstrm.parseLLMResponse
import com.example.minstrm.DeviceInfo
import android.content.Context
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Preview
@Composable
fun AddDeviceScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var produkt by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var effekt by remember { mutableStateOf("") }
    var estimeretTid by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
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


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(40.dp))

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

        Spacer(modifier = Modifier.height(32.dp))

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(8.dp))
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

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { /* Submit logic her */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007BFF))
        ) {
            Text("Tilføj enhed", color = Color.White)
        }
    }
}
