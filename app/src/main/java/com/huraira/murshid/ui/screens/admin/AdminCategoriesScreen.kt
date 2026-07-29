package com.huraira.murshid.ui.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.huraira.murshid.ui.components.GoldFilledButton
import com.huraira.murshid.ui.components.MurshidTopBar
import com.huraira.murshid.ui.theme.MurshidError
import com.huraira.murshid.ui.theme.MurshidGold
import com.huraira.murshid.ui.theme.MurshidSurface
import com.huraira.murshid.ui.theme.MurshidSurfaceElevated
import com.huraira.murshid.viewmodel.admin.AdminCategoriesViewModel

@Composable
fun AdminCategoriesScreen(
    onBack: () -> Unit,
    viewModel: AdminCategoriesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var newCategoryName by remember { mutableStateOf("") }
    var pendingDeleteCategory by remember { mutableStateOf<String?>(null) }
    var deleteErrorMessage by remember { mutableStateOf<String?>(null) }
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
                title = "Admin · Categories",
                showBack = true,
                onBack = onBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Categories apply only to Wallpapers. At least one must always exist.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MurshidGold.copy(alpha = 0.8f)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = { newCategoryName = it },
                        label = { Text("New category") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = adminTextFieldColors()
                    )
                    GoldFilledButton(
                        text = if (uiState.isSubmitting) "Adding…" else "Add",
                        onClick = {
                            if (newCategoryName.isNotBlank()) {
                                viewModel.addCategory(newCategoryName.trim())
                                newCategoryName = ""
                            }
                        }
                    )
                }
                if (uiState.addErrorMessage != null) {
                    Text(
                        text = uiState.addErrorMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MurshidError
                    )
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.categories, key = { it }) { category ->
                    val count = uiState.wallpaperCountByCategory[category] ?: 0
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MurshidSurface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = category,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White
                                )
                                Text(
                                    text = if (count == 1) "1 wallpaper" else "$count wallpapers",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MurshidGold.copy(alpha = 0.7f)
                                )
                            }
                            IconButton(onClick = {
                                deleteErrorMessage = null
                                pendingDeleteCategory = category
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Delete $category",
                                    tint = MurshidError
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    pendingDeleteCategory?.let { category ->
        DeleteCategoryDialog(
            categoryName = category,
            wallpaperCount = uiState.wallpaperCountByCategory[category] ?: 0,
            isLastCategory = uiState.categories.size <= 1,
            errorMessage = deleteErrorMessage,
            onConfirm = { password ->
                viewModel.deleteCategory(category, password) { success, error ->
                    if (success) {
                        pendingDeleteCategory = null
                        deleteErrorMessage = null
                    } else {
                        deleteErrorMessage = error
                    }
                }
            },
            onDismiss = {
                pendingDeleteCategory = null
                deleteErrorMessage = null
            }
        )
    }
}

@Composable
private fun DeleteCategoryDialog(
    categoryName: String,
    wallpaperCount: Int,
    isLastCategory: Boolean,
    errorMessage: String?,
    onConfirm: (password: String) -> Unit,
    onDismiss: () -> Unit
) {
    var password by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MurshidSurfaceElevated,
        title = { Text("Delete \"$categoryName\"?", color = Color.White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = if (wallpaperCount > 0) {
                        "This permanently deletes this category and all $wallpaperCount " +
                            (if (wallpaperCount == 1) "wallpaper" else "wallpapers") +
                            " in it. This can't be undone."
                    } else {
                        "This category has no wallpapers. Deleting it can't be undone."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f)
                )
                if (isLastCategory) {
                    Text(
                        text = "At least one category must remain — add another before deleting this one.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MurshidError
                    )
                } else {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Admin password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth(),
                        colors = adminTextFieldColors()
                    )
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MurshidError
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(password) },
                enabled = !isLastCategory
            ) {
                Text("Delete", color = if (isLastCategory) MurshidError.copy(alpha = 0.4f) else MurshidError)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MurshidGold)
            }
        }
    )
}
