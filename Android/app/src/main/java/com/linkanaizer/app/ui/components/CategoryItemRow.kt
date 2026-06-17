package com.linkanaizer.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.linkanaizer.app.ui.theme.*

@Composable
fun CategoryItemRow(
    title: String,
    count: Int,
    isVisible: Boolean,
    onToggle: () -> Unit,
    showDivider: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .clickable(onClick = onToggle)
                .padding(horizontal = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "$count",
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(Modifier.width(12.dp))
            Icon(
                imageVector = if (isVisible) Icons.Default.CheckBox
                else Icons.Default.CheckBoxOutlineBlank,
                contentDescription = if (isVisible) "Visible" else "Hidden",
                tint = if (isVisible) PrimaryLight else IconGray,
                modifier = Modifier.size(24.dp),
            )
        }
        if (showDivider) {
            HorizontalDivider(color = BorderLight, thickness = 1.dp)
        }
    }
}
