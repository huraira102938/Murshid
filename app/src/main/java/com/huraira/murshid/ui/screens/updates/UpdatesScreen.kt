package com.huraira.murshid.ui.screens.updates

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.huraira.murshid.ui.components.MurshidTopBar
import com.huraira.murshid.ui.components.UpdateListItemCard
import com.huraira.murshid.viewmodel.UpdatesViewModel

@Composable
fun UpdatesScreen(
    onUpdateClick: (String) -> Unit,
    onShare: () -> Unit,
    onAbout: () -> Unit,
    // region ADMIN — remove before Play Store release
    onAdmin: (() -> Unit)? = null,
    // endregion
    viewModel: UpdatesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            MurshidTopBar(
                title = "Updates",
                onShare = onShare,
                onAbout = onAbout,
                // region ADMIN — remove before Play Store release
                onAdmin = onAdmin
                // endregion
            )
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
                    onClick = { onUpdateClick(update.id) }
                )
            }
        }
    }
}
