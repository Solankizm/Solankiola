package com.solankizm.ola.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solankizm.ola.data.Profile

/**
 * Circular rider avatar. Shows the photo if set, otherwise the first letter of
 * the name on an accent-tinted disc. Used on the dashboard and in Settings.
 */
@Composable
fun Avatar(
    profile: Profile,
    accent: Color,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .size(size)
            .clip(CircleShape)
            .background(accent.copy(alpha = 0.18f))
            .border(2.dp, accent.copy(alpha = 0.8f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        val photo = profile.photo
        if (photo != null) {
            Image(
                bitmap = photo,
                contentDescription = "Rider photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(size).clip(CircleShape),
            )
        } else {
            Text(
                text = profile.name.trim().take(1).uppercase().ifBlank { "R" },
                color = accent,
                fontSize = (size.value * 0.42f).sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
