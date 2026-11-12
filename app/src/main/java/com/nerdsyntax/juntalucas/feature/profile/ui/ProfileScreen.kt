package com.nerdsyntax.juntalucas.feature.profile.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import com.nerdsyntax.juntalucas.feature.paywall.ui.PaywallScreen
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Locale

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
    onNavigateToAccount: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val plan = rememberProfilePlan()
    val appVersion = remember(context) { profileAppVersion(context) }
    val scope = rememberCoroutineScope()

    var dialog by rememberSaveable { mutableStateOf<ProfileDialog?>(null) }
    var showPlans by rememberSaveable { mutableStateOf(false) }
    var feedback by rememberSaveable { mutableStateOf("") }
    var changingPlan by rememberSaveable { mutableStateOf(false) }
    var subscriptionToCancel by rememberSaveable { mutableStateOf<String?>(null) }
    var cancellationOwner by rememberSaveable { mutableStateOf<String?>(null) }
    var cancelling by remember { mutableStateOf(false) }
    var cancellationError by remember { mutableStateOf<String?>(null) }

    val canManagePlan = plan.subscriptionId != null &&
            (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

    fun openCancellation(changePlan: Boolean) {
        subscriptionToCancel = plan.subscriptionId ?: return
        cancellationOwner = plan.ownerUid
        changingPlan = changePlan
        cancellationError = null
        dialog = ProfileDialog.CANCEL_SUBSCRIPTION
    }

    LaunchedEffect(state.statusMessage) {
        state.statusMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            vm.clearStatusMessage()
        }
    }

    if (showPlans) {
        BackHandler { showPlans = false }
        PaywallScreen(
            onClose = { showPlans = false },
            onSelectPlan = {},
            currentPlanId = plan.id
        )
        return
    }

    when (dialog) {
        ProfileDialog.SUBSCRIPTION -> AlertDialog(
            onDismissRequest = { dialog = null },
            title = { Text("Mi suscripción") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Plan en AutonomIA: ${plan.label}",
                        fontWeight = FontWeight.SemiBold
                    )
                    if (canManagePlan) {
                        OutlinedButton(
                            onClick = { openCancellation(false) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Cancelar renovación")
                        }
                    }
                }
            },
            confirmButton = {
                if (canManagePlan) {
                    Button(onClick = { openCancellation(true) }) {
                        Text("Cambiar plan")
                    }
                } else if (plan.id == "FREE") {
                    Button(onClick = {
                        dialog = null
                        showPlans = true
                    }) {
                        Text("Ver planes")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { dialog = null }) { Text("Cerrar") }
            }
        )

        ProfileDialog.CANCEL_SUBSCRIPTION -> AlertDialog(
            onDismissRequest = {
                if (!cancelling) dialog = ProfileDialog.SUBSCRIPTION
            },
            properties = DialogProperties(
                dismissOnBackPress = !cancelling,
                dismissOnClickOutside = !cancelling
            ),
            title = {
                Text(if (changingPlan) "Cambiar plan" else "Cancelar renovación")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        if (changingPlan) {
                            "Cancelaremos tu renovación de prueba. Tu cuenta quedará en Gratis hasta que confirmemos el pago del nuevo plan."
                        } else {
                            "Cancelaremos tu renovación de prueba. Tu cuenta volverá a Gratis cuando Mercado Pago lo confirme."
                        }
                    )
                    cancellationError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = !cancelling && subscriptionToCancel != null,
                    onClick = confirm@{
                        val expectedId = subscriptionToCancel ?: return@confirm
                        val auth = FirebaseAuth.getInstance()
                        val owner = auth.currentUser?.uid

                        if (owner == null || owner != cancellationOwner) {
                            cancellationError =
                                "Tu sesión cambió. Vuelve a abrir Mi suscripción."
                            return@confirm
                        }

                        val openPlansAfter = changingPlan
                        cancelling = true
                        cancellationError = null

                        scope.launch {
                            try {
                                val result = FirebaseFunctions.getInstance("us-central1")
                                    .getHttpsCallable("cancelSandboxSubscription")
                                    .call(mapOf("expectedSubscriptionId" to expectedId))
                                    .await()

                                if (auth.currentUser?.uid != owner) return@launch

                                val data = result.data as? Map<*, *>
                                if (data?.get("cancelled") != true) {
                                    cancellationError =
                                        "Aún no pudimos confirmar la cancelación."
                                    return@launch
                                }

                                dialog = null
                                showPlans = openPlansAfter
                                Toast.makeText(
                                    context,
                                    "Renovación cancelada.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } catch (e: CancellationException) {
                                throw e
                            } catch (e: FirebaseFunctionsException) {
                                if (auth.currentUser?.uid == owner) {
                                    cancellationError =
                                        e.message ?: "No se pudo cancelar la renovación."
                                }
                            } catch (_: Exception) {
                                if (auth.currentUser?.uid == owner) {
                                    cancellationError =
                                        "No se pudo cancelar la renovación. Intenta de nuevo."
                                }
                            } finally {
                                cancelling = false
                            }
                        }
                    }
                ) {
                    Text(
                        when {
                            cancelling -> "Cancelando…"
                            changingPlan -> "Cancelar y ver planes"
                            else -> "Confirmar cancelación"
                        }
                    )
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !cancelling,
                    onClick = { dialog = ProfileDialog.SUBSCRIPTION }
                ) {
                    Text("Volver")
                }
            }
        )

        ProfileDialog.HELP -> ProfileTextDialog(
            title = "Centro de ayuda",
            body = "Ventas y gastos: usa Movimientos para registrar y revisar tus operaciones.\n\n" +
                    "Archivo CSV: expórtalo desde Perfil y ábrelo en Excel u otra hoja de cálculo.\n\n" +
                    "Suscripciones: entra a Mi suscripción para comparar los planes o gestionar una renovación.",
            onClose = { dialog = null },
            action = "Escribir comentario" to { dialog = ProfileDialog.FEEDBACK }
        )

        ProfileDialog.FEEDBACK -> AlertDialog(
            onDismissRequest = { dialog = ProfileDialog.HELP },
            title = { Text("Compartir un comentario") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Escribe tu comentario y elige la aplicación y el destinatario para compartirlo.")
                    OutlinedTextField(
                        value = feedback,
                        onValueChange = { feedback = it.take(2000) },
                        label = { Text("Tu comentario") },
                        minLines = 3,
                        maxLines = 6,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = feedback.isNotBlank(),
                    onClick = {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Comentario sobre AutonomIA")
                            putExtra(Intent.EXTRA_TEXT, feedback.trim())
                        }
                        if (context.openProfileIntent(
                                Intent.createChooser(intent, "Compartir comentario")
                            )
                        ) {
                            feedback = ""
                            dialog = null
                        }
                    }
                ) {
                    Text("Compartir")
                }
            },
            dismissButton = {
                TextButton(onClick = { dialog = ProfileDialog.HELP }) { Text("Volver") }
            }
        )

        ProfileDialog.ABOUT -> ProfileTextDialog(
            title = "Acerca de AutonomIA",
            body = "AutonomIA ayuda a pequeños negocios a registrar ventas y gastos, revisar sus indicadores y analizar sus datos con inteligencia artificial.\n\n" +
                    (appVersion?.let { "Versión instalada: $it" }
                        ?: "Versión instalada no disponible."),
            onClose = { dialog = null }
        )

        ProfileDialog.PRIVACY -> ProfileTextDialog(
            title = "Uso y privacidad",
            body = "El perfil muestra tus datos de cuenta y del negocio. Los archivos CSV pueden contener información financiera: compártelos solo con quien tú decidas.\n\n" +
                    "Para cambiar tu contraseña o gestionar tu cuenta, entra a Seguridad de la cuenta. Las tarjetas se ingresan en el checkout del proveedor.",
            onClose = { dialog = null }
        )

        ProfileDialog.LOGOUT -> AlertDialog(
            onDismissRequest = { dialog = null },
            title = { Text("¿Cerrar sesión?") },
            text = { Text("Tendrás que iniciar sesión nuevamente para acceder a tu negocio.") },
            confirmButton = {
                TextButton(onClick = {
                    dialog = null
                    vm.logout()
                }) {
                    Text("Cerrar sesión", color = RedLogout)
                }
            },
            dismissButton = {
                TextButton(onClick = { dialog = null }) { Text("Volver") }
            }
        )

        null -> Unit
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

    val rawInitials = state.name.trim().split(Regex("\\s+"))
        .mapNotNull { it.firstOrNull()?.toString() }
        .take(2)
        .joinToString("")
        .uppercase()
    val initials = rawInitials.ifEmpty { "-" }

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
            value = state.businessName
        )
        ProfileListItem(
            icon = Icons.Outlined.Category,
            iconTint = Color(0xFFF59E0B),
            title = "Rubro",
            value = state.rubro
        )
        ProfileListItem(
            icon = Icons.Outlined.TrackChanges,
            iconTint = Color(0xFFEC4899),
            title = "Meta mensual",
            value = state.metaMensual
        )
        ProfileListItem(
            icon = Icons.Outlined.Payments,
            iconTint = Color(0xFF10B981),
            title = "Moneda",
            value = state.moneda
        )
        ProfileListItem(
            icon = Icons.Outlined.Edit,
            iconTint = BrandPurple,
            title = "Editar negocio",
            onClick = vm::abrirEditarNegocio
        )

        SectionHeader("SUSCRIPCIONES")
        ProfileListItem(
            icon = Icons.Outlined.Payments,
            iconTint = BrandPurple,
            title = "Mi suscripción",
            value = plan.label,
            onClick = { dialog = ProfileDialog.SUBSCRIPTION }
        )

        SectionHeader("CONFIGURACIÓN")
        ProfileListItem(
            icon = Icons.Outlined.Notifications,
            iconTint = Color(0xFFF59E0B),
            title = "Notificaciones del dispositivo",
            onClick = { context.openProfileNotificationSettings() }
        )
        ProfileListItem(
            icon = Icons.Outlined.FileDownload,
            iconTint = Color(0xFF10B981),
            title = "Exportar movimientos (CSV)",
            onClick = { vm.exportarDatosCsv(context) }
        )

        SectionHeader("PRIVACIDAD Y SEGURIDAD")
        if (onNavigateToAccount != null) {
            ProfileListItem(
                icon = Icons.Outlined.Security,
                iconTint = Color(0xFF3B82F6),
                title = "Seguridad de la cuenta",
                onClick = onNavigateToAccount
            )
        }
        ProfileListItem(
            icon = Icons.Outlined.Description,
            iconTint = SectionTextGray,
            title = "Uso y privacidad",
            onClick = { dialog = ProfileDialog.PRIVACY }
        )

        SectionHeader("SOPORTE")
        ProfileListItem(
            icon = Icons.AutoMirrored.Outlined.HelpOutline,
            iconTint = Color(0xFF0EA5E9),
            title = "Centro de ayuda y comentarios",
            onClick = { dialog = ProfileDialog.HELP }
        )
        ProfileListItem(
            icon = Icons.Outlined.Info,
            iconTint = Color(0xFF8B5CF6),
            title = "Acerca de AutonomIA",
            value = appVersion?.let { "v$it" },
            onClick = { dialog = ProfileDialog.ABOUT }
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedButton(
            onClick = { dialog = ProfileDialog.LOGOUT },
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
            TextButton(onClick = onDismiss, enabled = !isSaving) {
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
            TextButton(onClick = onDismiss, enabled = !isSaving) {
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
    onClick: (() -> Unit)? = null
) {
    val clickModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    Column(modifier = Modifier.fillMaxWidth().then(clickModifier)) {
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
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
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
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(0.85f, fill = false).padding(start = 8.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            if (onClick != null) {
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

private enum class ProfileDialog {
    SUBSCRIPTION, CANCEL_SUBSCRIPTION, HELP, FEEDBACK, ABOUT, PRIVACY, LOGOUT
}

@Composable
private fun ProfileTextDialog(
    title: String,
    body: String,
    onClose: () -> Unit,
    action: Pair<String, () -> Unit>? = null
) {
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(title) },
        text = {
            Text(
                text = body,
                modifier = Modifier
                    .heightIn(max = 320.dp)
                    .verticalScroll(rememberScrollState())
            )
        },
        confirmButton = {
            if (action == null) {
                TextButton(onClick = onClose) { Text("Cerrar") }
            } else {
                Button(onClick = action.second) { Text(action.first) }
            }
        },
        dismissButton = {
            if (action != null) {
                TextButton(onClick = onClose) { Text("Volver") }
            }
        }
    )
}

private fun Context.openProfileIntent(intent: Intent, showFailure: Boolean = true): Boolean {
    try {
        startActivity(intent)
        return true
    } catch (_: ActivityNotFoundException) {
        // El dispositivo no tiene una app que maneje la acción.
    } catch (_: SecurityException) {
        // El dispositivo no permite abrir esta pantalla.
    }

    if (showFailure) {
        Toast.makeText(this, "No se pudo abrir esta opción.", Toast.LENGTH_SHORT).show()
    }
    return false
}

private fun Context.openProfileNotificationSettings() {
    if (Build.VERSION.SDK_INT >= 26) {
        val notifications = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        if (openProfileIntent(notifications, showFailure = false)) return
    }

    openProfileIntent(
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null))
    )
}

@Suppress("DEPRECATION")
private fun profileAppVersion(context: Context): String? = try {
    context.packageManager.getPackageInfo(context.packageName, 0).versionName
} catch (_: android.content.pm.PackageManager.NameNotFoundException) {
    null
}

private data class ProfilePlan(
    val id: String? = null,
    val label: String = "Consultando…",
    val subscriptionId: String? = null,
    val ownerUid: String? = null
)

@Composable
private fun rememberProfilePlan(): ProfilePlan {
    val auth = remember { FirebaseAuth.getInstance() }
    var uid by remember(auth) { mutableStateOf(auth.currentUser?.uid) }

    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { uid = it.currentUser?.uid }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    val owner = uid
    var plan by remember(owner) {
        mutableStateOf(
            if (owner == null) ProfilePlan(label = "Sin sesión") else ProfilePlan()
        )
    }

    DisposableEffect(auth, owner) {
        if (owner == null) return@DisposableEffect onDispose {}

        val listener = FirebaseFirestore.getInstance()
            .collection("users").document(owner)
            .collection("billing").document("current")
            .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
                if (auth.currentUser?.uid != owner) return@addSnapshotListener

                if (error != null || snapshot == null) {
                    plan = ProfilePlan(label = "No disponible")
                    return@addSnapshotListener
                }

                if (snapshot.metadata.isFromCache || snapshot.metadata.hasPendingWrites()) {
                    return@addSnapshotListener
                }

                val id = if (snapshot.exists()) {
                    (snapshot.get("plan") as? String)?.trim()?.uppercase(Locale.ROOT)
                } else {
                    "FREE"
                }

                val subscriptionId = (snapshot.get("subscriptionId") as? String)
                    ?.takeIf {
                        it.isNotBlank() && snapshot.get("origen") == "MERCADO_PAGO_SANDBOX"
                    }

                plan = when (id) {
                    "FREE" -> ProfilePlan("FREE", "Gratis", ownerUid = owner)
                    "PLUS" -> ProfilePlan("PLUS", "Plus", subscriptionId, owner)
                    "PRO" -> ProfilePlan("PRO", "Pro", subscriptionId, owner)
                    else -> ProfilePlan(label = "No disponible")
                }
            }

        onDispose { listener.remove() }
    }

    return plan
}