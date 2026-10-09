package com.mughalarts.gownordermanager.ui.screens

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mughalarts.gownordermanager.slip.SlipExporter
import com.mughalarts.gownordermanager.slip.SlipRenderer
import com.mughalarts.gownordermanager.viewmodel.OrderDetailsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun OrderDetailsScreen(
    orderId: Long,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    viewModel: OrderDetailsViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val order by remember(orderId) { viewModel.observeOrder(orderId) }
        .collectAsStateWithLifecycle(initialValue = null)
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }

    val currentOrder = order
    val currentSettings = settings

    // The Agreement Slip image. Rebuilt whenever the order or the business settings change.
    val slipBitmap by produceState<Bitmap?>(null, currentOrder, currentSettings) {
        value = currentOrder?.let { o ->
            withContext(Dispatchers.Default) {
                SlipRenderer.render(context, o, currentSettings)
            }
        }
    }
    val bitmap = slipBitmap

    if (showDeleteDialog && currentOrder != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete order?") },
            text = { Text("This will permanently delete ${currentOrder.orderNumber}.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteOrder(currentOrder) { onBack() }
                    }
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Agreement Slip",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        if (bitmap != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Agreement Slip",
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.FillWidth
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator()
            }
        }

        val ready = bitmap != null && currentOrder != null

        Button(
            onClick = {
                if (bitmap != null && currentOrder != null) {
                    scope.launch {
                        val chooser = withContext(Dispatchers.IO) {
                            SlipExporter.createShareIntent(
                                context,
                                bitmap,
                                "Agreement_Slip_" + currentOrder.orderNumber
                            )
                        }
                        context.startActivity(chooser)
                    }
                }
            },
            enabled = ready,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("SHARE SLIP", style = MaterialTheme.typography.titleMedium)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = onEdit,
                enabled = currentOrder != null,
                modifier = Modifier.weight(1f)
            ) {
                Text("Edit Order")
            }
            OutlinedButton(
                onClick = {
                    if (bitmap != null && currentOrder != null) {
                        scope.launch {
                            val saved = withContext(Dispatchers.IO) {
                                SlipExporter.saveToGallery(
                                    context,
                                    bitmap,
                                    "Agreement_Slip_" + currentOrder.orderNumber
                                )
                            }
                            val message = if (saved) {
                                "Saved to Pictures/GownOrderManager"
                            } else {
                                "Could not save the image. Use Share Slip instead."
                            }
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        }
                    }
                },
                enabled = ready,
                modifier = Modifier.weight(1f)
            ) {
                Text("Save Image")
            }
        }

        OutlinedButton(
            onClick = { showDeleteDialog = true },
            enabled = currentOrder != null,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Delete Order")
        }
    }
}
