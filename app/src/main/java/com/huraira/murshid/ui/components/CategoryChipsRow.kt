package com.huraira.murshid.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.huraira.murshid.ui.theme.MurshidBlack
import com.huraira.murshid.ui.theme.MurshidGold
import com.huraira.murshid.ui.theme.MurshidSurface

/**
 * Horizontal scrolling row of category chips.
 *
 * Used two ways:
 *  - User-facing filter (Wallpapers screen): [includeAllChip] = true, leading "All" chip,
 *    [selected] = null means "All" is active.
 *  - Admin category picker (Upload sheet): [includeAllChip] = false, exactly one existing
 *    category must be chosen.
 */
@Composable
fun CategoryChipsRow(
    categories: List<String>,
    selected: String?,
    onSelect: (String?) -> Unit,
    modifier: Modifier = Modifier,
    includeAllChip: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp)
) {
    LazyRow(
        modifier = modifier,
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (includeAllChip) {
            item {
                CategoryChip(label = "All", isSelected = selected == null, onClick = { onSelect(null) })
            }
        }
        items(categories) { category ->
            CategoryChip(
                label = category,
                isSelected = selected == category,
                onClick = { onSelect(category) }
            )
        }
    }
}

@Composable
private fun CategoryChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) MurshidGold else MurshidSurface)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (isSelected) MurshidBlack else MurshidGold
        )
    }
}
