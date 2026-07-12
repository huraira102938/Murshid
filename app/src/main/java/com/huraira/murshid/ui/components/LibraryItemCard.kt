package com.huraira.murshid.ui.components

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.huraira.murshid.data.model.LibraryContentType
import com.huraira.murshid.data.model.LibraryItem
import com.huraira.murshid.ui.theme.MurshidGold
import com.huraira.murshid.ui.theme.MurshidSurface

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LibraryItemCard(
    item: LibraryItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onLongClick: (() -> Unit)? = null,
    onImageClick: (LibraryItem) -> Unit = {}
) {
    val cardModifier = if (onLongClick != null) {
        modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
    } else {
        modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    }

    Card(
        modifier = cardModifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MurshidSurface)
    ) {
        when (item.type) {
            LibraryContentType.QUOTE -> QuoteContent(item)
            LibraryContentType.IMAGE -> ImageContent(item, onImageClick)
            LibraryContentType.IMAGE_QUOTE -> ImageQuoteContent(item, onImageClick)
            LibraryContentType.VIDEO -> VideoContent(item)
        }
    }
}

@Composable
private fun QuoteContent(item: LibraryItem) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var isFavorite by rememberSaveable(item.id) { mutableStateOf(false) }

    val fullQuote = "\u201C${item.quoteText.orEmpty()}\u201D" +
            if (!item.author.isNullOrBlank()) " — ${item.author}" else ""

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "\u201C${item.quoteText.orEmpty()}\u201D",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            if (!item.author.isNullOrBlank()) {
                Text(
                    text = "— ${item.author}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MurshidGold,
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(28.dp),
                modifier = Modifier.padding(top = 20.dp)
            ) {
                IconButton(onClick = {
                    clipboardManager.setText(AnnotatedString(fullQuote))
                    Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                }) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = "Copy", tint = MurshidGold)
                }
                IconButton(onClick = {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, fullQuote)
                    }
                    context.startActivity(Intent.createChooser(intent, "Share quote"))
                }) {
                    Icon(Icons.Filled.Share, contentDescription = "Share", tint = MurshidGold)
                }
                IconButton(onClick = { isFavorite = !isFavorite }) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = MurshidGold
                    )
                }
            }
        }
    }
}

@Composable
private fun ImageContent(item: LibraryItem, onImageClick: (LibraryItem) -> Unit) {
    AsyncImage(
        model = item.imageUrl,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(18.dp))
            .clickable { onImageClick(item) }
    )
}

@Composable
private fun ImageQuoteContent(item: LibraryItem, onImageClick: (LibraryItem) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f)
            .clickable { onImageClick(item) }
    ) {
        AsyncImage(
            model = item.imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                    )
                )
        )
        Text(
            text = "\u201C${item.quoteText.orEmpty()}\u201D",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp)
        )
    }
}

@Composable
private fun VideoContent(item: LibraryItem) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
        ) {
            AsyncImage(
                model = item.videoThumbnailUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.25f))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MurshidGold.copy(alpha = 0.9f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Play video",
                    tint = Color.Black
                )
            }
        }
        if (!item.quoteText.isNullOrBlank()) {
            Text(
                text = item.quoteText,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}