package com.huraira.murshid.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.huraira.murshid.R
import com.huraira.murshid.ui.theme.MurshidBlack
import com.huraira.murshid.ui.theme.MurshidGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MurshidTopBar(
    title: String,
    showBack: Boolean = false,
    onBack: () -> Unit = {},
    onShare: (() -> Unit)? = null,
    onRate: (() -> Unit)? = null,
    onAbout: (() -> Unit)? = null,
    // region ADMIN — remove before Play Store release
    onAdmin: (() -> Unit)? = null,
    // endregion
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

    TopAppBar(
        modifier = modifier,
        title = {
            androidx.compose.foundation.layout.Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = R.drawable.murshid,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }
        },
        navigationIcon = {
            if (showBack) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MurshidGold
                    )
                }
            }
        },
        actions = {
            if (onShare != null || onRate != null || onAbout != null || onAdmin != null) {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "More options",
                        tint = MurshidGold
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    if (onShare != null) {
                        DropdownMenuItem(
                            text = { Text("Share") },
                            leadingIcon = { Icon(Icons.Filled.Share, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onShare()
                            }
                        )
                    }
                    if (onRate != null) {
                        DropdownMenuItem(
                            text = { Text("Rate Murshid") },
                            leadingIcon = { Icon(Icons.Filled.Star, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onRate()
                            }
                        )
                    }
                    if (onAbout != null) {
                        DropdownMenuItem(
                            text = { Text("About") },
                            leadingIcon = { Icon(Icons.Filled.Info, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onAbout()
                            }
                        )
                    }
                    // region ADMIN — remove before Play Store release
                    if (onAdmin != null) {
                        DropdownMenuItem(
                            text = { Text("Admin") },
                            leadingIcon = { Icon(Icons.Filled.AdminPanelSettings, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onAdmin()
                            }
                        )
                    }
                    // endregion
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MurshidBlack,
            titleContentColor = androidx.compose.ui.graphics.Color.White,
            navigationIconContentColor = MurshidGold,
            actionIconContentColor = MurshidGold
        )
    )
}