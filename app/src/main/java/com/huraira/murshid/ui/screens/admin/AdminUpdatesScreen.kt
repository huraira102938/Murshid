package com.huraira.murshid.ui.screens.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.huraira.murshid.data.model.UpdateItem
import com.huraira.murshid.ui.components.ConfirmDeleteDialog
import com.huraira.murshid.ui.components.FullScreenImagePreviewDialog
import com.huraira.murshid.ui.components.GoldFilledButton
import com.huraira.murshid.ui.components.MurshidTopBar
import com.huraira.murshid.ui.components.UpdateListItemCard
import com.huraira.murshid.ui.theme.MurshidGold
import com.huraira.murshid.ui.theme.MurshidSurface
import com.huraira.murshid.ui.theme.MurshidSurfaceElevated
import com.huraira.murshid.viewmodel.admin.AdminUpdatesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUpdatesScreen(
    onBack: () -> Unit,
    viewModel: AdminUpdatesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateSheet by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<UpdateItem?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.consumeError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            MurshidTopBar(
                title = "Admin · Updates",
                showBack = true,
                onBack = onBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateSheet = true },
                containerColor = MurshidGold,
                contentColor = Color.Black
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Create update")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(uiState.updates, key = { it.id }) { update ->
                UpdateListItemCard(
                    update = update,
                    onClick = {},
                    onLongClick = { pendingDelete = update }
                )
            }
        }
    }

    if (showCreateSheet) {
        CreateUpdateSheet(
            isSubmitting = uiState.isSubmitting,
            onDismiss = { showCreateSheet = false },
            onSubmit = { title, date, summary, fullContent, detailImageUri, youtubeVideoId ->
                viewModel.create(title, date, summary, fullContent, detailImageUri, youtubeVideoId)
                showCreateSheet = false
            }
        )
    }

    pendingDelete?.let { update ->
        ConfirmDeleteDialog(
            itemLabel = "\"${update.title}\"",
            onConfirm = { viewModel.delete(update.id) },
            onDismiss = { pendingDelete = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateUpdateSheet(
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (
        title: String,
        date: String,
        summary: String,
        fullContent: String,
        detailImageUri: Uri?,
        youtubeVideoId: String?
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var summary by remember { mutableStateOf("") }
    var fullContent by remember { mutableStateOf("") }
    var youtubeVideoId by remember { mutableStateOf("") }
    var detailImageUri by remember { mutableStateOf<Uri?>(null) }
    var showFullPreview by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) detailImageUri = uri }

    val canSubmit = title.isNotBlank() && date.isNotBlank() &&
            summary.isNotBlank() && fullContent.isNotBlank()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MurshidSurfaceElevated
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Create Update",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
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
                value = date,
                onValueChange = { date = it },
                label = { Text("Date (e.g. July 8, 2026)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = adminTextFieldColors()
            )
            OutlinedTextField(
                value = summary,
                onValueChange = { summary = it },
                label = { Text("Summary") },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth(),
                colors = adminTextFieldColors()
            )
            OutlinedTextField(
                value = fullContent,
                onValueChange = { fullContent = it },
                label = { Text("Full content") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                minLines = 3,
                maxLines = 8,
                colors = adminTextFieldColors()
            )

            Text(
                text = "Detail image (optional)",
                style = MaterialTheme.typography.labelLarge,
                color = MurshidGold
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MurshidSurface)
                    .then(
                        if (detailImageUri != null) {
                            Modifier.clickable { showFullPreview = true }
                        } else {
                            Modifier
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (detailImageUri != null) {
                    AsyncImage(
                        model = detailImageUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Icon(
                        imageVector = Icons.Filled.Fullscreen,
                        contentDescription = "Tap to preview full screen",
                        tint = Color.White,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                    )
                } else {
                    Icon(Icons.Filled.Image, contentDescription = null, tint = MurshidGold)
                }
            }

            if (showFullPreview && detailImageUri != null) {
                FullScreenImagePreviewDialog(
                    imageModel = detailImageUri,
                    onDismiss = { showFullPreview = false }
                )
            }

            GoldFilledButton(
                text = if (detailImageUri == null) "Choose Image" else "Change Image",
                onClick = {
                    imagePicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = youtubeVideoId,
                onValueChange = { youtubeVideoId = it },
                label = { Text("YouTube video ID (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = adminTextFieldColors()
            )

            GoldFilledButton(
                text = if (isSubmitting) "Saving…" else "Create",
                onClick = {
                    if (canSubmit) {
                        onSubmit(
                            title.trim(),
                            date.trim(),
                            summary.trim(),
                            fullContent.trim(),
                            detailImageUri,
                            youtubeVideoId.trim().ifBlank { null }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
