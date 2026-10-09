package com.mughalarts.gownordermanager.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mughalarts.gownordermanager.ui.components.SectionCard
import com.mughalarts.gownordermanager.ui.theme.SuccessGreen
import com.mughalarts.gownordermanager.viewmodel.SettingsViewModel

/** Settings screen (this file name is kept from Stage 1 so the upload overwrites it). */
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = viewModel()) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val saved by viewModel.saved.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        SectionCard("BUSINESS INFORMATION") {
            OutlinedTextField(
                value = settings.businessName,
                onValueChange = viewModel::onBusinessName,
                label = { Text("Business Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = settings.phone,
                onValueChange = viewModel::onPhone,
                label = { Text("Phone Number") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = settings.address,
                onValueChange = viewModel::onAddress,
                label = { Text("Address") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
        }

        SectionCard("SLIP SETTINGS") {
            Text(
                text = "Business Logo",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "The MUGHAL ARTS logo is built into the Agreement Slip.",
                style = MaterialTheme.typography.bodySmall
            )
            OutlinedTextField(
                value = settings.footerText,
                onValueChange = viewModel::onFooterText,
                label = { Text("Footer Text") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Button(
            onClick = { viewModel.save() },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("SAVE SETTINGS")
        }
        if (saved) {
            Text(
                text = "Settings saved.",
                color = SuccessGreen,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        SectionCard("APP INFORMATION") {
            Text(
                text = "About Gown Order Manager",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "An offline app to record gown orders, track Pending and Paid payments, and prepare customer slips.",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Version 1.0",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
