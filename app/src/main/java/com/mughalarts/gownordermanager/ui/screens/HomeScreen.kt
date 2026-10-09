package com.mughalarts.gownordermanager.ui.screens

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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mughalarts.gownordermanager.ui.components.OrderCard
import com.mughalarts.gownordermanager.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    onNewOrder: () -> Unit,
    onOrderClick: (Long) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val totalOrders by viewModel.totalOrders.collectAsStateWithLifecycle()
    val pendingOrders by viewModel.pendingOrders.collectAsStateWithLifecycle()
    val paidOrders by viewModel.paidOrders.collectAsStateWithLifecycle()
    val totalGowns by viewModel.totalGowns.collectAsStateWithLifecycle()
    val recentOrders by viewModel.recentOrders.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Gown Order Manager",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Manage your gown orders easily",
                style = MaterialTheme.typography.bodyLarge
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Total Orders", totalOrders.toString(), Modifier.weight(1f))
            StatCard("Pending", pendingOrders.toString(), Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Paid", paidOrders.toString(), Modifier.weight(1f))
            StatCard("Total Gowns", totalGowns.toString(), Modifier.weight(1f))
        }

        Button(
            onClick = onNewOrder,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("+ New Order", style = MaterialTheme.typography.titleMedium)
        }

        Text(
            text = "Recent Orders",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        val recent = recentOrders
        if (recent != null) {
            if (recent.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "No orders yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Create your first gown order.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Button(onClick = onNewOrder) {
                            Text("+ New Order")
                        }
                    }
                }
            } else {
                recent.forEach { order ->
                    OrderCard(order = order, onClick = { onOrderClick(order.id) })
                }
            }
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
