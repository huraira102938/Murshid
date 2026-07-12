package com.huraira.murshid.ui.screens.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.huraira.murshid.ui.components.MurshidTopBar
import com.huraira.murshid.ui.theme.MurshidGold
import com.huraira.murshid.ui.theme.MurshidSurface

private data class AdminSection(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
fun AdminHomeScreen(
    onBack: () -> Unit,
    onWallpapers: () -> Unit,
    onLibrary: () -> Unit,
    onUpdates: () -> Unit,
    onNotifications: () -> Unit
) {
    val sections = listOf(
        AdminSection("Wallpapers", Icons.Filled.Wallpaper, onWallpapers),
        AdminSection("Library", Icons.Filled.AutoStories, onLibrary),
        AdminSection("Updates", Icons.Filled.NewReleases, onUpdates),
        AdminSection("Notifications", Icons.Filled.Notifications, onNotifications)
    )

    Scaffold(
        topBar = {
            MurshidTopBar(
                title = "Admin",
                showBack = true,
                onBack = onBack
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(sections) { section ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = section.onClick),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MurshidSurface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = section.icon,
                            contentDescription = null,
                            tint = MurshidGold
                        )
                        Text(
                            text = section.label,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = MurshidGold
                        )
                    }
                }
            }
        }
    }
}
