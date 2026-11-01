package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CaramelWarm,
    onPrimary = CoffeeDarkRoast,
    primaryContainer = CoffeeMocha,
    onPrimaryContainer = LightCaramel,
    secondary = HoneyBronze,
    onSecondary = CoffeeDarkRoast,
    secondaryContainer = CoffeeBean,
    onSecondaryContainer = LightCaramel,
    tertiary = LightCaramel,
    onTertiary = CoffeeEspresso,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = CoffeeMocha,
    onPrimary = Color.White,
    primaryContainer = LatteFoam,
    onPrimaryContainer = CoffeeDarkRoast,
    secondary = CaramelAmber,
    onSecondary = Color.White,
    secondaryContainer = LightCaramel,
    onSecondaryContainer = CoffeeEspresso,
    tertiary = CoffeeBean,
    onTertiary = Color.White,
    background = CreamWhite,
    onBackground = TextDarkEspresso,
    surface = SurfaceCardLight,
    onSurface = TextDarkEspresso,
    surfaceVariant = LatteFoam,
    onSurfaceVariant = TextMutedRoast,
    outline = BorderSubtle
)

@Composable
fun BackyardBaristaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
