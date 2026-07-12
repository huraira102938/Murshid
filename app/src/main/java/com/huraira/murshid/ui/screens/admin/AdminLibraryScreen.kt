package com.huraira.murshid.ui.screens.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import com.huraira.murshid.data.model.LibraryContentType
import com.huraira.murshid.data.model.LibraryItem
import com.huraira.murshid.ui.components.ConfirmDeleteDialog
import com.huraira.murshid.ui.components.GoldFilledButton
import com.huraira.murshid.ui.components.GoldOutlinedButton
import com.huraira.murshid.ui.components.LibraryItemCard
import com.huraira.murshid.ui.components.MurshidTopBar
import com.huraira.murshid.ui.theme.MurshidGold
import com.huraira.murshid.ui.theme.MurshidSurface
import com.huraira.murshid.ui.theme.MurshidSurfaceElevated
import com.huraira.murshid.viewmodel.admin.AdminLibraryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLibraryScreen(
    onBack: () -> Unit,
    viewModel: AdminLibraryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateSheet by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<LibraryItem?>(null) }

    Scaffold(
        topBar = {
            MurshidTopBar(
                title = "Admin · Library",
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
                Icon(Icons.Filled.Add, contentDescription = "Create library item")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(uiState.items, key = { it.id }) { item ->
                LibraryItemCard(
                    item = item,
                    onClick = {},
                    onLongClick = { pendingDelete = item }
                )
            }
        }
    }

    if (showCreateSheet) {
        CreateLibraryItemSheet(
            isSubmitting = uiState.isSubmitting,
            onDismiss = { showCreateSheet = false },
            onSubmit = { item ->
                viewModel.create(item)
                showCreateSheet = false
            }
        )
    }

    pendingDelete?.let { item ->
        ConfirmDeleteDialog(
            itemLabel = "this library item",
            onConfirm = { viewModel.delete(item.id) },
            onDismiss = { pendingDelete = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateLibraryItemSheet(
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (LibraryItem) -> Unit
) {
    var selectedType by remember { mutableStateOf(LibraryContentType.QUOTE) }
    var quoteText by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) selectedImageUri = uri }

    val needsQuote = selectedType == LibraryContentType.QUOTE || selectedType == LibraryContentType.IMAGE_QUOTE
    val needsImage = selectedType == LibraryContentType.IMAGE || selectedType == LibraryContentType.IMAGE_QUOTE

    val canSubmit = when (selectedType) {
        LibraryContentType.QUOTE -> quoteText.isNotBlank()
        LibraryContentType.IMAGE -> selectedImageUri != null
        LibraryContentType.IMAGE_QUOTE -> quoteText.isNotBlank() && selectedImageUri != null
        LibraryContentType.VIDEO -> false // not offered by this create form
    }

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
                text = "Create Library Item",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )

            Row3TypeSelector(selectedType) { selectedType = it }

            if (needsImage) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MurshidSurface),
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.Image, contentDescription = null, tint = MurshidGold)
                            Text(
                                text = "No image selected",
                                style = MaterialTheme.typography.bodySmall,
                                color = MurshidGold,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }

                GoldFilledButton(
                    text = if (selectedImageUri == null) "Choose Image" else "Change Image",
                    onClick = {
                        imagePicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (needsQuote) {
                OutlinedTextField(
                    value = quoteText,
                    onValueChange = { quoteText = it },
                    label = { Text("Quote text") },
                    minLines = 2,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth(),
                    colors = adminTextFieldColors()
                )
                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text("Author (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = adminTextFieldColors()
                )
            }

            GoldFilledButton(
                text = if (isSubmitting) "Saving…" else "Create",
                onClick = {
                    if (canSubmit) {
                        onSubmit(
                            LibraryItem(
                                id = "",
                                type = selectedType,
                                quoteText = quoteText.trim().ifBlank { null },
                                author = author.trim().ifBlank { null },
                                imageUrl = selectedImageUri?.toString()
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun Row3TypeSelector(
    selected: LibraryContentType,
    onSelect: (LibraryContentType) -> Unit
) {
    val options = listOf(
        LibraryContentType.QUOTE to "Quote",
        LibraryContentType.IMAGE to "Image only",
        LibraryContentType.IMAGE_QUOTE to "Image + Quote"
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { (type, label) ->
            if (selected == type) {
                GoldFilledButton(
                    text = label,
                    onClick = { onSelect(type) },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                GoldOutlinedButton(
                    text = label,
                    onClick = { onSelect(type) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
