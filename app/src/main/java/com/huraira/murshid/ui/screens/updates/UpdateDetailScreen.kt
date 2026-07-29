package com.huraira.murshid.ui.screens.updates

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.huraira.murshid.data.model.UpdateItem
import com.huraira.murshid.ui.components.MurshidAsyncImage
import com.huraira.murshid.ui.components.MurshidTopBar
import com.huraira.murshid.ui.theme.MurshidGold
import com.huraira.murshid.viewmodel.UpdatesViewModel

@Composable
fun UpdateDetailScreen(
    updateId: String,
    onBack: () -> Unit,
    onShare: () -> Unit,
    viewModel: UpdatesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val update: UpdateItem? = uiState.updates.firstOrNull { it.id == updateId }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            MurshidTopBar(
                title = "Update",
                showBack = true,
                onBack = onBack,
                onShare = onShare
            )
        }
    ) { innerPadding ->
        if (update == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (uiState.isLoading) "Loading…" else "Update not found.",
                    color = Color.White
                )
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            val imageUrl = update.detailImageUrl ?: update.thumbnailUrl
            if (!imageUrl.isNullOrBlank()) {
                MurshidAsyncImage(
                    model = imageUrl,
                    contentDescription = update.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(16.dp))
                )
            }

            Text(
                text = update.date.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MurshidGold,
                modifier = Modifier.padding(top = 20.dp)
            )
            Text(
                text = update.title,
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                modifier = Modifier.padding(top = 6.dp, bottom = 16.dp)
            )
            Text(
                text = update.fullContent,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )

            if (!update.youtubeVideoId.isNullOrBlank()) {
                Text(
                    text = "Watch on YouTube",
                    style = MaterialTheme.typography.labelLarge,
                    color = MurshidGold,
                    modifier = Modifier
                        .padding(top = 24.dp)
                        .fillMaxWidth()
                        .clickable {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://www.youtube.com/watch?v=${update.youtubeVideoId}")
                            )
                            context.startActivity(intent)
                        }
                )
            }
        }
    }
}