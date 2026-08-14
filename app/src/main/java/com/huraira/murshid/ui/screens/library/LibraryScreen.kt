package com.huraira.murshid.ui.screens.library

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.huraira.murshid.data.model.LibraryContentType
import com.huraira.murshid.data.model.LibraryItem
import com.huraira.murshid.ui.components.LibraryImagePagerDialog
import com.huraira.murshid.ui.components.LibraryItemCard
import com.huraira.murshid.ui.components.LibraryListShimmer
import com.huraira.murshid.ui.components.LoadErrorState
import com.huraira.murshid.ui.components.MurshidTopBar
import com.huraira.murshid.viewmodel.LibraryViewModel

@Composable
fun LibraryScreen(
    onShare: () -> Unit,
    onAbout: () -> Unit,
    // region ADMIN — remove before Play Store release
    onAdmin: (() -> Unit)? = null,
    // endregion
    viewModel: LibraryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var expandedItem by remember { mutableStateOf<LibraryItem?>(null) }

    // Only images/image-quotes are pageable in the fullscreen viewer.
    val imageItems = remember(uiState.items) {
        uiState.items.filter {
            (it.type == LibraryContentType.IMAGE || it.type == LibraryContentType.IMAGE_QUOTE) &&
                    !it.imageUrl.isNullOrBlank()
        }
    }

    Scaffold(
        topBar = {
            MurshidTopBar(
                title = "Murshid Library",
                onShare = onShare,
                onAbout = onAbout,
                // region ADMIN — remove before Play Store release
                onAdmin = onAdmin
                // endregion
            )
        }
    ) { innerPadding ->
        when {
            uiState.isLoading && uiState.items.isEmpty() -> {
                LibraryListShimmer(modifier = Modifier.padding(innerPadding))
            }
            uiState.errorMessage != null && uiState.items.isEmpty() -> {
                LoadErrorState(
                    message = uiState.errorMessage!!,
                    onRetry = { viewModel.refresh() },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.padding(innerPadding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.items, key = { it.id }) { item ->
                        LibraryItemCard(
                            item = item,
                            onClick = {
                                if (item.type == LibraryContentType.VIDEO && !item.videoUrl.isNullOrBlank()) {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.videoUrl))
                                    context.startActivity(intent)
                                }
                            },
                            onImageClick = { clicked -> expandedItem = clicked }
                        )
                    }
                }
            }
        }
    }

    val currentExpanded = expandedItem
    if (currentExpanded != null) {
        val startIndex = imageItems.indexOfFirst { it.id == currentExpanded.id }.coerceAtLeast(0)
        LibraryImagePagerDialog(
            items = imageItems,
            startIndex = startIndex,
            onDismiss = { expandedItem = null }
        )
    }
}
