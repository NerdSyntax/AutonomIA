package com.nerdsyntax.juntalucas.feature.profile.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DarkBlue = Color(0xFF0F2A4A)
private val LightGrayBg = Color(0xFFF8FAFC)
private val SectionTextGray = Color(0xFF94A3B8)
private val DividerColor = Color(0xFFF1F5F9)
private val PurpleBadge = Color(0xFF8B5CF6)
private val RedLogout = Color(0xFFDC2626)

@Composable
fun ProfileScreen(
    state: ProfileUiState,
    vm: ProfileViewModel,
    onNavigateToAccount: () -> Unit = {}
) {
    val rawInitials = state.name.split(" ")
        .mapNotNull { it.firstOrNull()?.toString() }
        .take(2)
        .joinToString("")
        .uppercase()

    val initials = if (rawInitials.isNotEmpty()) rawInitials else "-"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkBlue)
                .padding(top = 48.dp, bottom = 32.dp, start = 24.dp, end = 24.dp)
        ) {
            Column {
                Text("Perfil", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(initials, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)

                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(x = (-4).dp, y = (-4).dp)
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(PurpleBadge),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(state.name.ifEmpty { "Cargando..." }, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(state.email, color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                        Text(state.businessName, color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                    }

                    Surface(
                        color = Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.clickable { }
                    ) {
                        Text("Editar", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                    }
                }
            }
        }

        SectionHeader("MI NEGOCIO")
        ProfileListItem(icon = Icons.Default.Store, iconTint = Color(0xFFEF4444), title = "Nombre del negocio", value = state.businessName)
        ProfileListItem(icon = Icons.Default.Folder, iconTint = Color(0xFFF59E0B), title = "Rubro", value = state.rubro)
        ProfileListItem(icon = Icons.Default.TrackChanges, iconTint = Color(0xFFEC4899), title = "Meta mensual", value = state.metaMensual)
        ProfileListItem(icon = Icons.Default.Payments, iconTint = Color(0xFF10B981), title = "Moneda", value = state.moneda)
        ProfileListItem(icon = Icons.Default.Edit, iconTint = Color(0xFFF97316), title = "Editar información del negocio", showArrow = true)

        SectionHeader("CONFIGURACIÓN")
        ProfileListItem(icon = Icons.Default.LocalOffer, iconTint = Color(0xFFF59E0B), title = "Categorías de gastos", showArrow = true)
        ProfileListItem(icon = Icons.Default.Loop, iconTint = Color(0xFF3B82F6), title = "Gastos recurrentes", showArrow = true)
        ProfileListItem(icon = Icons.Default.Notifications, iconTint = Color(0xFFF59E0B), title = "Notificaciones", switchState = state.notificacionesEnabled, onSwitchChange = vm::onNotificacionesChange)
        ProfileListItem(icon = Icons.Default.Lock, iconTint = Color(0xFFF59E0B), title = "Biometría", switchState = state.biometriaEnabled, onSwitchChange = vm::onBiometriaChange)

        SectionHeader("PRIVACIDAD Y SEGURIDAD")
        ProfileListItem(icon = Icons.Default.VpnKey, iconTint = Color(0xFFF59E0B), title = "Cambiar contraseña", showArrow = true, onClick = onNavigateToAccount)
        ProfileListItem(icon = Icons.Default.Security, iconTint = Color(0xFF3B82F6), title = "Privacidad y datos", showArrow = true)
        ProfileListItem(icon = Icons.Default.Devices, iconTint = Color(0xFF8B5CF6), title = "Sesiones activas", showArrow = true)

        SectionHeader("SOPORTE")
        ProfileListItem(icon = Icons.Default.HelpOutline, iconTint = Color(0xFFEF4444), title = "Centro de ayuda", showArrow = true)
        ProfileListItem(icon = Icons.Default.ChatBubbleOutline, iconTint = Color(0xFFA855F7), title = "Enviar comentario", showArrow = true)
        ProfileListItem(icon = Icons.Default.Info, iconTint = Color(0xFF3B82F6), title = "Acerca de AutonomIA", value = "v1.0.0", showArrow = true)
        ProfileListItem(icon = Icons.Default.Description, iconTint = Color(0xFF94A3B8), title = "Términos y condiciones", showArrow = true)

        Spacer(modifier = Modifier.height(24.dp))
        OutlinedButton(
            onClick = { vm.logout() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(54.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, RedLogout),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = RedLogout)
        ) {
            Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Cerrar sesión", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun SectionHeader(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(LightGrayBg)
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Text(
            text = title,
            color = SectionTextGray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ProfileListItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    value: String? = null,
    showArrow: Boolean = false,
    switchState: Boolean? = null,
    onSwitchChange: ((Boolean) -> Unit)? = null,
    onClick: () -> Unit = {}
) {
    Column(modifier = Modifier.clickable(enabled = switchState == null) { onClick() }) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(16.dp))

            Text(title, color = DarkBlue, fontSize = 15.sp, modifier = Modifier.weight(1f))

            if (!value.isNullOrEmpty()) {
                Text(value, color = SectionTextGray, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(8.dp))
            }
            if (switchState != null && onSwitchChange != null) {
                Switch(
                    checked = switchState,
                    onCheckedChange = onSwitchChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = DarkBlue,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFFE2E8F0),
                        uncheckedBorderColor = Color.Transparent
                    )
                )
            } else if (showArrow) {
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(20.dp))
            }
        }
        HorizontalDivider(color = DividerColor, thickness = 1.dp)
    }
}