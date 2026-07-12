package com.huraira.murshid.ui.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import android.widget.Toast
import com.huraira.murshid.ui.components.GoldFilledButton
import com.huraira.murshid.ui.components.MurshidTopBar
import com.huraira.murshid.ui.theme.MurshidGold
import com.huraira.murshid.viewmodel.admin.AdminNotificationsViewModel

@Composable
fun AdminNotificationsScreen(
    onBack: () -> Unit,
    viewModel: AdminNotificationsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }

    LaunchedEffect(uiState.lastSendSucceeded) {
        when (uiState.lastSendSucceeded) {
            true -> {
                Toast.makeText(context, "Notification sent", Toast.LENGTH_SHORT).show()
                title = ""
                body = ""
                viewModel.consumeSendResult()
            }
            false -> {
                Toast.makeText(context, "Failed to send. Try again.", Toast.LENGTH_SHORT).show()
                viewModel.consumeSendResult()
            }
            null -> Unit
        }
    }

    Scaffold(
        topBar = {
            MurshidTopBar(
                title = "Admin · Notifications",
                showBack = true,
                onBack = onBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Send a push notification to everyone with the app installed.",
                style = MaterialTheme.typography.bodyMedium,
                color = MurshidGold
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = adminTextFieldColors()
            )

            OutlinedTextField(
                value = body,
                onValueChange = { body = it },
                label = { Text("Body") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
                colors = adminTextFieldColors()
            )

            GoldFilledButton(
                text = if (uiState.isSending) "Sending…" else "Send",
                onClick = {
                    if (title.isNotBlank() && body.isNotBlank()) {
                        viewModel.send(title.trim(), body.trim())
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
