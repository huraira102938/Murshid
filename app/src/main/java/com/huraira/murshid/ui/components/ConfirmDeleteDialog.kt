package com.huraira.murshid.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.huraira.murshid.ui.theme.MurshidError
import com.huraira.murshid.ui.theme.MurshidGold
import com.huraira.murshid.ui.theme.MurshidSurfaceElevated

/**
 * Themed confirmation dialog used by every admin delete action (wallpapers, library
 * items, updates). Keeps the confirm pattern identical everywhere it's used.
 */
@Composable
fun ConfirmDeleteDialog(
    itemLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MurshidSurfaceElevated,
        title = { Text("Delete $itemLabel?", color = Color.White) },
        text = { Text("This can't be undone.", color = Color.White.copy(alpha = 0.7f)) },
        confirmButton = {
            TextButton(onClick = {
                onDismiss()
                onConfirm()
            }) {
                Text("Delete", color = MurshidError)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MurshidGold)
            }
        }
    )
}
