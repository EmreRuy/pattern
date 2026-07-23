package com.example.pattern.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pattern.ui.utils.PatternIconMapper

@Composable
fun PatternIcon(
    iconCode: String,
    modifier: Modifier = Modifier,
    size: TextUnit = 24.sp,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    if (iconCode.startsWith("system:")) {
        val iconName = iconCode.removePrefix("system:")
        val imageVector = PatternIconMapper.getIcon(iconName)
        Icon(
            imageVector = imageVector,
            contentDescription = null,
            tint = color,
            modifier = modifier.size(size.value.dp)
        )
    } else {
        Text(
            text = iconCode,
            fontSize = size,
            modifier = modifier
        )
    }
}
