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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
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
import com.huraira.murshid.data.model.WallpaperItem
import com.huraira.murshid.ui.components.CategoryChipsRow
import com.huraira.murshid.ui.components.ConfirmDeleteDialog
import com.huraira.murshid.ui.components.FullScreenImagePreviewDialog
import com.huraira.murshid.ui.components.GoldFilledButton
import com.huraira.murshid.ui.components.MurshidTopBar
import com.huraira.murshid.ui.components.WallpaperThumbnail
import com.huraira.murshid.ui.theme.MurshidError
import com.huraira.murshid.ui.theme.MurshidGold
import com.huraira.murshid.ui.theme.MurshidSurface
import com.huraira.murshid.ui.theme.MurshidSurfaceElevated
import com.huraira.murshid.viewmodel.admin.AdminWallpapersViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminWallpapersScreen(
    onBack: () -> Unit,
    onManageCategories: () -> Unit,
    viewModel: AdminWallpapersViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showUploadSheet by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<WallpaperItem?>(null) }
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
                title = "Admin · Wallpapers",
                showBack = true,
                onBack = onBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showUploadSheet = true },
                containerColor = MurshidGold,
                contentColor = Color.Black
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Upload wallpaper")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CategoryChipsRow(
                    categories = uiState.categories,
                    selected = uiState.selectedCategory,
                    onSelect = { viewModel.selectCategory(it) },
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onManageCategories) {
                    Icon(
                        imageVector = Icons.Filled.Tune,
                        contentDescription = "Manage categories",
                        tint = MurshidGold
                    )
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.visibleWallpapers, key = { it.id }) { wallpaper ->
                    WallpaperThumbnail(
                        wallpaper = wallpaper,
                        onClick = {},
                        onLongClick = { pendingDelete = wallpaper }
                    )
                }
            }
        }
    }

    if (showUploadSheet) {
        UploadWallpaperSheet(
            categories = uiState.categories,
            isSubmitting = uiState.isSubmitting,
            onDismiss = { showUploadSheet = false },
            onSubmit = { title, category, uri ->
                viewModel.upload(title, category, uri)
                showUploadSheet = false
            }
        )
    }

    pendingDelete?.let { wallpaper ->
        ConfirmDeleteDialog(
            itemLabel = "\"${wallpaper.title}\"",
            onConfirm = { viewModel.delete(wallpaper.id) },
            onDismiss = { pendingDelete = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UploadWallpaperSheet(
    categories: List<String>,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (title: String, category: String, imageUri: Uri) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember(categories) { mutableStateOf(categories.firstOrNull()) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showFullPreview by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) selectedImageUri = uri }

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
                text = "Upload Wallpaper",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MurshidSurface)
                    .then(
                        if (selectedImageUri != null) {
                            Modifier.clickable { showFullPreview = true }
                        } else {
                            Modifier
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    AsyncImage(
                        model = selectedImageUri,
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

            if (showFullPreview && selectedImageUri != null) {
                FullScreenImagePreviewDialog(
                    imageModel = selectedImageUri,
                    onDismiss = { showFullPreview = false },
                    simulateWallpaperCrop = true
                )
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

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = adminTextFieldColors()
            )

            Text(
                text = "Category",
                style = MaterialTheme.typography.labelLarge,
                color = MurshidGold
            )
            if (categories.isEmpty()) {
                Text(
                    text = "No categories yet — add one from Manage Categories first.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MurshidError
                )
            } else {
                CategoryChipsRow(
                    categories = categories,
                    selected = selectedCategory,
                    onSelect = { selectedCategory = it },
                    includeAllChip = false,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            GoldFilledButton(
                text = if (isSubmitting) "Uploading…" else "Upload",
                onClick = {
                    val uri = selectedImageUri
                    val category = selectedCategory
                    if (title.isNotBlank() && category != null && uri != null) {
                        onSubmit(title.trim(), category, uri)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
internal fun adminTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = MurshidGold,
    unfocusedBorderColor = MurshidGold.copy(alpha = 0.4f),
    focusedLabelColor = MurshidGold,
    unfocusedLabelColor = MurshidGold.copy(alpha = 0.7f),
    cursorColor = MurshidGold
)
