package com.solankizm.ola.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ---- Solankizm brand palette ----------------------------------------------
object Brand {
    val Eco = Color(0xFF2ED47A)      // ride mode: Eco / default accent
    val Normal = Color(0xFF3B9CFF)   // ride mode: Normal
    val Sports = Color(0xFFFF8A1F)   // ride mode: Sports
    val Hyper = Color(0xFFFF3B4E)    // ride mode: Hyper

    val Ink = Color(0xFF050607)      // darkest background
    val Panel = Color(0xFF0E1113)    // card surface (dark)
    val PanelHi = Color(0xFF151A1D)  // raised card (dark)
    val Line = Color(0xFF1C2125)     // hairline divider (dark)
    val Text = Color(0xFFF4F6F7)
    val Dim = Color(0xFF7D878E)

    // light theme
    val InkL = Color(0xFFEDEFF1)
    val PanelL = Color(0xFFFFFFFF)
    val PanelHiL = Color(0xFFF5F7F8)
    val LineL = Color(0xFFE1E5E8)
    val TextL = Color(0xFF0B0D0E)
    val DimL = Color(0xFF69737A)
}

private val DarkColors = darkColorScheme(
    primary = Brand.Eco,
    onPrimary = Brand.Ink,
    background = Brand.Ink,
    onBackground = Brand.Text,
    surface = Brand.Panel,
    onSurface = Brand.Text,
    surfaceVariant = Brand.PanelHi,
    onSurfaceVariant = Brand.Dim,
    outline = Brand.Line,
)

private val LightColors = lightColorScheme(
    primary = Brand.Eco,
    onPrimary = Color.White,
    background = Brand.InkL,
    onBackground = Brand.TextL,
    surface = Brand.PanelL,
    onSurface = Brand.TextL,
    surfaceVariant = Brand.PanelHiL,
    onSurfaceVariant = Brand.DimL,
    outline = Brand.LineL,
)

private val AppType = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 140.sp,
        letterSpacing = (-4).sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        letterSpacing = 1.5.sp,
    ),
)

@Composable
fun SolankizmTheme(
    dark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        typography = AppType,
        content = content,
    )
}
