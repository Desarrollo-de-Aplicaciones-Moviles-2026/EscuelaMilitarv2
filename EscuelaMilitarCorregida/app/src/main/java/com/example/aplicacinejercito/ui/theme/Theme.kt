package com.example.aplicacinejercito.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Claro = lightColorScheme(
    primary = AzulInstitucional, onPrimary = Color.White,
    primaryContainer = Color(0xFFE8EDF4), onPrimaryContainer = AzulInstitucional,
    secondary = DoradoInstitucional, onSecondary = AzulInstitucional,
    secondaryContainer = Color(0xFFF9E8B9), onSecondaryContainer = AzulInstitucional,
    tertiary = VerdeEstado, onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE2F3E9), onTertiaryContainer = Color(0xFF14532F),
    background = FondoClaro, surface = Color.White,
    surfaceVariant = Color(0xFFE8EDF4), onSurfaceVariant = Color(0xFF4C6079),
    outline = Color(0xFFB6C4D5), outlineVariant = Color(0xFFDCE4EE),
    onBackground = Color(0xFF0E1B2B), onSurface = Color(0xFF0E1B2B)
)
private val Oscuro = darkColorScheme(
    primary = Color(0xFFACC8EF), onPrimary = AzulInstitucional,
    primaryContainer = Color(0xFF223E5D), onPrimaryContainer = Color(0xFFD7E7FC),
    secondary = DoradoInstitucional, onSecondary = Color(0xFF302500),
    secondaryContainer = Color(0xFF51401A), onSecondaryContainer = Color(0xFFFFE29A),
    tertiary = Color(0xFF8AD6AB), onTertiary = Color(0xFF003921),
    tertiaryContainer = Color(0xFF165136), onTertiaryContainer = Color(0xFFB4F0CE),
    background = Color(0xFF101923), surface = Color(0xFF182431),
    onBackground = Color(0xFFE0E8F2), onSurface = Color(0xFFE0E8F2)
)

@Composable
fun AplicacinEjercitoTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (darkTheme) Oscuro else Claro, typography = Typography, shapes = AppShapes, content = content)
}
