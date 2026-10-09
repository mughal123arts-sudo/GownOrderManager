package com.mughalarts.gownordermanager.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mughalarts.gownordermanager.ui.components.InfoRow
import com.mughalarts.gownordermanager.ui.components.SectionCard
import com.mughalarts.gownordermanager.ui.components.StatusBadge
import com.mughalarts.gownordermanager.util.formatDate
import com.mughalarts.gownordermanager.util.formatMoney
import com.mughalarts.gownordermanager.viewmodel.GownColor
import com.mughalarts.gownordermanager.viewmodel.GownColorState
import com.mughalarts.gownordermanager.viewmodel.NewOrderViewModel

/**
 * Used for both a new order (orderId = null) and editing a saved order (orderId = its id).
 * [onSaved] receives the id of the saved order.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewOrderScreen(
    orderId: Long?,
    onSaved: (Long) -> Unit,
    viewModel: NewOrderViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val orderNumber by viewModel.orderNumber.collectAsStateWithLifecycle()
    val orderDate by viewModel.orderDate.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }
    val isEditing = orderId != null

    LaunchedEffect(orderId) {
        viewModel.loadOrder(orderId)
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = state.deliveryDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onDeliveryDate(pickerState.selectedDateMillis)
                        showDatePicker = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = if (isEditing) "Edit Order" else "New Order",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "All fields are optional.",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        SectionCard("CUSTOMER INFORMATION") {
            OutlinedTextField(
                value = state.customerName,
                onValueChange = viewModel::onCustomerName,
                label = { Text("Customer Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.schoolName,
                onValueChange = viewModel::onSchoolName,
                label = { Text("School / College Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.contactNumber,
                onValueChange = viewModel::onContactNumber,
                label = { Text("Contact Number") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.address,
                onValueChange = viewModel::onAddress,
                label = { Text("Address") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
        }

        SectionCard("ORDER INFORMATION") {
            InfoRow("Order Number", orderNumber, valueBold = true)
            InfoRow("Order Date", formatDate(orderDate))
            Text(
                text = "Delivery Date",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                val date = state.deliveryDate
                Text(if (date == null) "Select delivery date" else formatDate(date))
            }
        }

        SectionCard("GOWN COLORS") {
            GownColor.entries.forEach { color ->
                GownColorRow(
                    color = color,
                    colorState = state.colors.getValue(color),
                    onSelectedChange = { viewModel.onColorSelected(color, it) },
                    onQuantityChange = { viewModel.onColorQuantity(color, it) }
                )
            }
            HorizontalDivider()
            Text(
                text = "Total Gowns: ${state.totalGowns}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        SectionCard("PAYMENT") {
            OutlinedTextField(
                value = state.totalAmount,
                onValueChange = viewModel::onTotalAmount,
                label = { Text("Total Amount (Rs)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.advanceAmount,
                onValueChange = viewModel::onAdvanceAmount,
                label = { Text("Advance Payment (Rs)") },
                singleLine = true,
                isError = state.advanceTooHigh,
                supportingText = {
                    if (state.advanceTooHigh) {
                        Text("Advance cannot be more than the total amount")
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            InfoRow(
                label = "Remaining Balance",
                value = formatMoney(state.remainingBalance),
                valueBold = true
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Payment Status",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                StatusBadge(state.paymentStatus)
            }
        }

        SectionCard("SPECIAL INSTRUCTIONS") {
            OutlinedTextField(
                value = state.specialInstructions,
                onValueChange = viewModel::onSpecialInstructions,
                label = { Text("Special Instructions") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
        }

        val errorMessage = state.errorMessage
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Button(
            onClick = { viewModel.saveOrder(onSaved) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                text = if (isEditing) "SAVE CHANGES" else "SAVE ORDER",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun GownColorRow(
    color: GownColor,
    colorState: GownColorState,
    onSelectedChange: (Boolean) -> Unit,
    onQuantityChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = colorState.selected,
            onCheckedChange = onSelectedChange
        )
        Text(
            text = color.label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge
        )
        OutlinedTextField(
            value = colorState.quantity,
            onValueChange = onQuantityChange,
            enabled = colorState.selected,
            label = { Text("Qty") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.width(110.dp)
        )
    }
}
