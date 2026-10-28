package com.nerdsyntax.juntalucas.feature.profile.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DarkBlue = Color(0xFF0F2A4A)
private val LightGrayBg = Color(0xFFF8FAFC)
private val SectionTextGray = Color(0xFF94A3B8)
private val TextGray = Color(0xFF475569)
private val DividerColor = Color(0xFFF1F5F9)
private val RedLogout = Color(0xFFDC2626)

private val BrandPurple = Color(0xFF7C3AED)
private val GradientPurpleStart = Color(0xFF6366F1)
private val GradientPurpleEnd = Color(0xFF9333EA)

private val ProfilePurpleGradient = Brush.horizontalGradient(
    colors = listOf(GradientPurpleStart, GradientPurpleEnd)
)

@Composable
fun ProfileScreen(
    state: ProfileUiState,
    vm: ProfileViewModel,
    onNavigateToAccount: () -> Unit = {}
) {
    val context = LocalContext.current

    LaunchedEffect(state.statusMessage) {
        state.statusMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            vm.clearStatusMessage()
        }
    }

    if (state.isEditingUser) {
        EditUserDialog(
            name = state.editNameInput,
            email = state.email,
            isSaving = state.isSaving,
            onNameChange = vm::onEditNameChange,
            onDismiss = vm::cerrarEditarUsuario,
            onSave = vm::guardarDatosUsuario
        )
    }

    if (state.isEditingBusiness) {
        EditBusinessDialog(
            businessName = state.editBusinessNameInput,
            rubro = state.editRubroInput,
            metaMensual = state.editMetaInput,
            isSaving = state.isSaving,
            onBusinessNameChange = vm::onEditBusinessNameChange,
            onRubroChange = vm::onEditRubroChange,
            onMetaChange = vm::onEditMetaChange,
            onDismiss = vm::cerrarEditarNegocio,
            onSave = vm::guardarDatosNegocio
        )
    }

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
                .background(
                    color = DarkBlue,
                    shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                )
                .padding(top = 48.dp, bottom = 32.dp, start = 24.dp, end = 24.dp)
        ) {
            Column {
                Text(
                    text = "Mi Perfil",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(28.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(ProfilePurpleGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = state.name.ifEmpty { "Cargando..." },
                            color = Color.White,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = state.email,
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 13.sp
                        )
                    }

                    Surface(
                        color = Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.clickable { vm.abrirEditarUsuario() }
                    ) {
                        Text(
                            text = "Editar",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        SectionHeader("MI NEGOCIO")
        ProfileListItem(
            icon = Icons.Outlined.Storefront,
            iconTint = Color(0xFFEF4444),
            title = "Nombre del negocio",
            value = state.businessName,
        )
        ProfileListItem(
            icon = Icons.Outlined.Category,
            iconTint = Color(0xFFF59E0B),
            title = "Rubro",
            value = state.rubro,
        )
        ProfileListItem(
            icon = Icons.Outlined.TrackChanges,
            iconTint = Color(0xFFEC4899),
            title = "Meta mensual",
            value = state.metaMensual,
        )
        ProfileListItem(
            icon = Icons.Outlined.Payments,
            iconTint = Color(0xFF10B981),
            title = "Moneda",
            value = state.moneda
        )

        SectionHeader("CONFIGURACIÓN")
        ProfileListItem(
            icon = Icons.Outlined.Notifications,
            iconTint = Color(0xFFF59E0B),
            title = "Notificaciones",
            switchState = state.notificacionesEnabled,
            onSwitchChange = vm::onNotificacionesChange
        )

        ProfileListItem(
            icon = Icons.Outlined.FileDownload,
            iconTint = Color(0xFF10B981),
            title = "Exportar movimientos (Excel/CSV)",
            showArrow = true,
            onClick = {
                vm.exportarDatosCsv(context)
            }
        )

        SectionHeader("PRIVACIDAD Y SEGURIDAD")
        ProfileListItem(
            icon = Icons.Outlined.Security,
            iconTint = Color(0xFF3B82F6),
            title = "Seguridad de la cuenta",
            showArrow = true,
            onClick = onNavigateToAccount
        )

        SectionHeader("SOPORTE")
        ProfileListItem(
            icon = Icons.Outlined.HelpOutline,
            iconTint = Color(0xFF0EA5E9),
            title = "Centro de ayuda y comentarios",
            showArrow = true
        )
        ProfileListItem(
            icon = Icons.Outlined.Info,
            iconTint = Color(0xFF8B5CF6),
            title = "Acerca de AutonomIA",
            value = "v1.0.0",
            showArrow = true
        )
        ProfileListItem(
            icon = Icons.Outlined.Description,
            iconTint = SectionTextGray,
            title = "Términos y condiciones",
            showArrow = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedButton(
            onClick = { vm.logout() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, RedLogout.copy(alpha = 0.5f)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = RedLogout)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Logout,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text("Cerrar sesión", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}


@Composable
private fun EditUserDialog(
    name: String,
    email: String,
    isSaving: Boolean,
    onNameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = "Editar perfil",
                color = DarkBlue,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("Tu nombre", color = TextGray) },
                    singleLine = true,
                    enabled = !isSaving,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandPurple,
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = {},
                    label = { Text("Correo electrónico", color = TextGray) },
                    singleLine = true,
                    enabled = false,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = Color(0xFFE2E8F0),
                        disabledTextColor = TextGray.copy(alpha = 0.7f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSaving
            ) {
                Text("Cancelar", color = SectionTextGray, fontWeight = FontWeight.Bold)
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                enabled = !isSaving,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkBlue)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Text("Guardar", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    )
}

@Composable
private fun EditBusinessDialog(
    businessName: String,
    rubro: String,
    metaMensual: String,
    isSaving: Boolean,
    onBusinessNameChange: (String) -> Unit,
    onRubroChange: (String) -> Unit,
    onMetaChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = "Editar negocio",
                color = DarkBlue,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = businessName,
                    onValueChange = onBusinessNameChange,
                    label = { Text("Nombre del negocio", color = TextGray) },
                    singleLine = true,
                    enabled = !isSaving,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandPurple,
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = rubro,
                    onValueChange = onRubroChange,
                    label = { Text("Rubro", color = TextGray) },
                    singleLine = true,
                    enabled = !isSaving,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandPurple,
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = metaMensual,
                    onValueChange = onMetaChange,
                    label = { Text("Meta mensual (CLP)", color = TextGray) },
                    prefix = { Text("$", color = DarkBlue) },
                    singleLine = true,
                    enabled = !isSaving,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandPurple,
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSaving
            ) {
                Text("Cancelar", color = SectionTextGray, fontWeight = FontWeight.Bold)
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                enabled = !isSaving,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkBlue)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Text("Guardar", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    )
}

@Composable
private fun SectionHeader(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(LightGrayBg)
            .padding(horizontal = 24.dp, vertical = 14.dp)
    ) {
        Text(
            text = title,
            color = SectionTextGray,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
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
                .padding(horizontal = 24.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconTint.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = title,
                color = DarkBlue,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

            if (!value.isNullOrEmpty()) {
                Text(
                    text = value,
                    color = TextGray,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            if (switchState != null && onSwitchChange != null) {
                Switch(
                    checked = switchState,
                    onCheckedChange = onSwitchChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = BrandPurple,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFFCBD5E1),
                        uncheckedBorderColor = Color.Transparent
                    ),
                    modifier = Modifier.height(24.dp)
                )
            } else if (showArrow) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color(0xFFCBD5E1),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        HorizontalDivider(color = DividerColor, thickness = 1.dp)
    }
}