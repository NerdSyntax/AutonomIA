package com.nerdsyntax.juntalucas.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ReceiptLong

private data class BottomDestination(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

private val bottomDestinations = listOf(
    BottomDestination(Routes.DASHBOARD, "Inicio", Icons.Default.Home),
    BottomDestination(Routes.MOVEMENTS, "Movimientos", Icons.Default.ReceiptLong),
    BottomDestination(Routes.BUSINESS, "Negocio", Icons.Default.Business),
    BottomDestination(Routes.AI, "IA", Icons.Default.AutoAwesome),
    BottomDestination(Routes.PROFILE, "Perfil", Icons.Default.AccountCircle)
)

internal val bottomRoutes = bottomDestinations.mapTo(mutableSetOf()) { it.route }

@Composable
internal fun AppBottomNavigation(currentRoute: String?, onNavigate: (String) -> Unit) {
    NavigationBar {
        bottomDestinations.forEach { destination ->
            NavigationBarItem(
                selected = currentRoute == destination.route,
                onClick = { onNavigate(destination.route) },
                icon = { Icon(destination.icon, contentDescription = destination.label) },
                label = { Text(destination.label) }
            )
        }
    }
}
