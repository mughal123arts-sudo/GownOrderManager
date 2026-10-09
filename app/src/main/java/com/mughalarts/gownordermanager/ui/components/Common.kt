package com.mughalarts.gownordermanager.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mughalarts.gownordermanager.data.Order
import com.mughalarts.gownordermanager.ui.theme.PaidBackground
import com.mughalarts.gownordermanager.ui.theme.PendingBackground
import com.mughalarts.gownordermanager.ui.theme.PendingText
import com.mughalarts.gownordermanager.ui.theme.SuccessGreen
import com.mughalarts.gownordermanager.util.PaymentStatus
import com.mughalarts.gownordermanager.util.formatDateOrDash
import com.mughalarts.gownordermanager.util.formatMoney

@Composable
fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            content()
        }
    }
}

@Composable
fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueBold: Boolean = false
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (valueBold) FontWeight.Bold else FontWeight.Medium
        )
    }
}

/** Shows only "Pending" or "Paid". */
@Composable
fun StatusBadge(status: String, modifier: Modifier = Modifier) {
    val paid = status == PaymentStatus.PAID
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = if (paid) PaidBackground else PendingBackground
    ) {
        Text(
            text = if (paid) PaymentStatus.PAID else PaymentStatus.PENDING,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (paid) SuccessGreen else PendingText
        )
    }
}

@Composable
fun OrderCard(
    order: Order,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = order.orderNumber,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                StatusBadge(order.paymentStatus)
            }
            Text(
                text = order.customerName.ifBlank { "No name" },
                style = MaterialTheme.typography.titleMedium
            )
            InfoRow("School / College", order.schoolName.ifBlank { "-" })
            InfoRow("Total Gowns", order.totalGowns.toString())
            InfoRow("Total Amount", formatMoney(order.totalAmount))
            InfoRow("Delivery Date", formatDateOrDash(order.deliveryDate))
        }
    }
}
