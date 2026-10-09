package com.mughalarts.gownordermanager.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mughalarts.gownordermanager.ui.components.OrderCard
import com.mughalarts.gownordermanager.viewmodel.OrderFilter
import com.mughalarts.gownordermanager.viewmodel.OrdersViewModel

@Composable
fun OrdersScreen(
    onOrderClick: (Long) -> Unit,
    viewModel: OrdersViewModel = viewModel()
) {
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp)
    ) {
        Text(
            text = "Saved Orders",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        OutlinedTextField(
            value = query,
            onValueChange = viewModel::onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            placeholder = { Text("Search customer or order number") },
            singleLine = true
        )

        Row(
            modifier = Modifier.padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OrderFilter.entries.forEach { option ->
                FilterChip(
                    selected = filter == option,
                    onClick = { viewModel.onFilterChange(option) },
                    label = { Text(option.label) }
                )
            }
        }

        val list = orders
        if (list != null) {
            if (list.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No orders found.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(list, key = { it.id }) { order ->
                        OrderCard(order = order, onClick = { onOrderClick(order.id) })
                    }
                }
            }
        }
    }
}
