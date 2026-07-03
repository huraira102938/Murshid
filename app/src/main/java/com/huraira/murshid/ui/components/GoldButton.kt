package com.huraira.murshid.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.huraira.murshid.ui.theme.MurshidBlack
import com.huraira.murshid.ui.theme.MurshidGold

@Composable
fun GoldFilledButton(
    text: String,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = MurshidGold,
            contentColor = MurshidBlack
        )
    ) {
        Row {
            if (icon != null) {
                Icon(imageVector = icon, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
            }
            Text(text = text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun GoldOutlinedButton(
    text: String,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, MurshidGold),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MurshidGold)
    ) {
        Row {
            if (icon != null) {
                Icon(imageVector = icon, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
            }
            Text(text = text, style = MaterialTheme.typography.labelLarge)
        }
    }
}