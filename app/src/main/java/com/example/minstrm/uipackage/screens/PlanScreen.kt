package com.example.minstrm.uipackage.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minstrm.model.Device

@Composable
fun PlanScreen(
    devices: List<Device>,
    onAddDeviceClick: () -> Unit,
    onEditClick: (Int) -> Unit,
    onDeleteClick: (Int) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(bottom = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(45.dp))
            Text(
                text = "Planlæg",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp, bottom = 16.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .padding(horizontal = 16.dp)
                    .background(Color.LightGray, shape = RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("Skema placeholder", color = Color.Black)
            }

            SmartTilfoejCard(onAddDeviceClick)

            Spacer(modifier = Modifier.height(8.dp))

            devices.forEachIndexed { idx, device ->
                DeviceCard(
                    device = device,
                    onEditClick = { onEditClick(idx) },
                    onDeleteClick = { onDeleteClick(idx) }
                )
            }
        }
    }
}

@Composable
fun SmartTilfoejCard(onAddDeviceClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF007BFF)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("✨ Smart tilføj", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("Tilføj enheder ved at tage et billede", color = Color.White)
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onAddDeviceClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Text("Prøv det", color = Color(0xFF007BFF))
            }
        }
    }
}

@Composable
fun DeviceCard(
    device: Device,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = device.produkt,
                color = Color(0xFF007BFF),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Effekt: ${device.effekt}", fontWeight = FontWeight.SemiBold)
            Text("Tid: ${device.estimeretTid}", fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onEditClick) {
                    Text("Rediger enhed")
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Slet enhed",
                        tint = Color.Red
                    )
                }
            }
        }
    }
}
