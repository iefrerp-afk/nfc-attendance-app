package pk.edu.nfc.attendance.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = NfcBluePrimary,
    onPrimary = Color.White,
    secondary = NfcBlueSecondary,
    onSecondary = Color.White,
    tertiary = NfcGoldAccent,
    background = NfcBackgroundLight,
    surface = NfcSurfaceLight,
    onSurface = Color(0xFF1A1C1E),
    error = StatusAbsentRed
)

private val DarkColorScheme = darkColorScheme(
    primary = NfcBlueSecondary,
    onPrimary = Color.White,
    secondary = NfcGoldAccent,
    onSecondary = Color.Black,
    background = NfcBackgroundDark,
    surface = NfcSurfaceDark,
    onSurface = Color(0xFFE2E2E6),
    error = StatusAbsentRed
)

@Composable
fun NfcAttendanceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
