package com.nerdsyntax.juntalucas.feature.paywall.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Locale

private val DarkBlue = Color(0xFF0F2A4A)
private val LightBg = Color(0xFFF8FAFC)
private val GradientPurpleStart = Color(0xFF6366F1)
private val GradientPurpleEnd = Color(0xFF9333EA)
private val PurpleBrush = Brush.horizontalGradient(
    listOf(GradientPurpleStart, GradientPurpleEnd)
)

data class PlanUiModel(
    val id: String,
    val name: String,
    val description: String,
    val price: String,
    val period: String,
    val isPopular: Boolean = false,
    val isCurrent: Boolean = false,
    val features: List<String>,
    val amountClp: Long = 0L
)

@Suppress("UNUSED_PARAMETER")
@Composable
fun PaywallScreen(
    onClose: () -> Unit,
    onSelectPlan: (String) -> Unit,
    currentPlanId: String? = null,
    onContinueFree: (() -> Unit)? = null,
    isContinuing: Boolean = false,
    onboardingError: String? = null
) {
    val context = LocalContext.current
    val isOnboarding = onContinueFree != null
    val canOpenTestCheckout = !isOnboarding &&
            (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var creatingCheckout by remember { mutableStateOf(false) }

    BackHandler(enabled = isOnboarding && isContinuing) {
        // Espera a que termine el guardado del registro.
    }

    LaunchedEffect(isOnboarding, onboardingError) {
        if (isOnboarding && !onboardingError.isNullOrBlank()) {
            snackbarHostState.showSnackbar(onboardingError)
        }
    }

    val normalizedCurrentPlan = currentPlanId
        ?.trim()
        ?.uppercase(Locale.ROOT)
        .takeUnless { isOnboarding }
    val alreadyPaid = normalizedCurrentPlan == "PLUS" || normalizedCurrentPlan == "PRO"

    val planes = listOf(
        PlanUiModel(
            id = "FREE",
            name = "AutonomIA Gratis",
            description = "Obtén ayuda esencial para ordenar tus finanzas básicas.",
            price = "CLP 0",
            period = "por mes",
            isCurrent = normalizedCurrentPlan == "FREE",
            features = listOf(
                "Catálogo de hasta 50 productos",
                "100 registros de ventas/gastos al mes",
                "15 consultas mensuales a la IA",
                "1 diagnóstico ejecutivo al cierre de mes",
                "Exportación CSV del mes actual"
            )
        ),
        PlanUiModel(
            id = "PLUS",
            name = "AutonomIA Plus",
            description = "Obtén acceso adicional a potentes funciones de IA para crecer.",
            price = "CLP 4.990",
            period = "por mes",
            isPopular = true,
            isCurrent = normalizedCurrentPlan == "PLUS",
            amountClp = 4_990L,
            features = listOf(
                "Todo lo del plan Gratis, además de:",
                "Catálogo de hasta 500 productos",
                "Ventas y gastos ilimitados",
                "50 consultas mensuales a la IA",
                "4 diagnósticos ejecutivos al mes",
                "Exportación CSV de los últimos 6 meses"
            )
        ),
        PlanUiModel(
            id = "PRO",
            name = "AutonomIA Pro",
            description = "El CFO definitivo en tu bolsillo. Sin límites, máximo control.",
            price = "CLP 12.990",
            period = "por mes",
            isCurrent = normalizedCurrentPlan == "PRO",
            amountClp = 12_990L,
            features = listOf(
                "Todo lo del plan Plus, además de:",
                "Catálogo y movimientos ilimitados",
                "Consultas a la IA ilimitadas",
                "Diagnósticos y simulaciones a demanda",
                "Historial de análisis interanual",
                "Exportación CSV sin restricciones históricas"
            )
        )
    )

    Scaffold(
        containerColor = LightBg,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .background(
                            color = DarkBlue,
                            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                        )
                        .padding(top = 48.dp, bottom = 28.dp, start = 20.dp, end = 20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Suscripciones",
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = if (isOnboarding) {
                                    "Empieza gratis y revisa los planes de pago desde tu perfil"
                                } else {
                                    "Elige el plan que mejor se adapte a tu negocio"
                                },
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }

                        Spacer(Modifier.width(12.dp))
                        IconButton(
                            onClick = onClose,
                            enabled = !isContinuing,
                            modifier = Modifier.size(40.dp)
                                .background(Color.White.copy(alpha = 0.12f), CircleShape)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            items(planes, key = { it.id }) { plan ->
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    PlanCard(
                        plan = plan,
                        actionEnabled = if (isOnboarding) {
                            plan.id == "FREE" && !isContinuing
                        } else {
                            canOpenTestCheckout && !alreadyPaid && !creatingCheckout &&
                                    (plan.id == "PLUS" || plan.id == "PRO")
                        },
                        actionLabel = if (isOnboarding) "Empezar gratis" else "Continuar al pago",
                        showFreeAction = isOnboarding,
                        unavailableLabel = when {
                            !isOnboarding && creatingCheckout -> "Preparando enlace…"
                            !isOnboarding && alreadyPaid -> "Ya tienes un plan de pago"
                            !isOnboarding -> "No disponible por ahora"
                            plan.id == "FREE" -> "Guardando…"
                            else -> "Disponible desde Perfil"
                        },
                        onSelect = openCheckout@{
                            if (onContinueFree != null) {
                                if (plan.id == "FREE" && !isContinuing) onContinueFree()
                                return@openCheckout
                            }

                            if (!canOpenTestCheckout || creatingCheckout || alreadyPaid) {
                                return@openCheckout
                            }

                            creatingCheckout = true
                            scope.launch {
                                val auth = FirebaseAuth.getInstance()
                                val uid = auth.currentUser?.uid
                                try {
                                    check(uid != null) {
                                        "Inicia sesión para continuar al pago."
                                    }

                                    val result = FirebaseFunctions.getInstance("us-central1")
                                        .getHttpsCallable("createSandboxCheckout")
                                        .call(mapOf("plan" to plan.id))
                                        .await()

                                    if (auth.currentUser?.uid != uid) return@launch

                                    val data = result.data as? Map<*, *>
                                    val url = data?.get("checkoutUrl") as? String
                                        ?: error("No se recibió el enlace de pago.")
                                    val uri = Uri.parse(url)

                                    check(
                                        uri.scheme == "https" &&
                                                (uri.host == "www.mercadopago.cl" ||
                                                        uri.host == "www.mercadopago.com") &&
                                                uri.path == "/subscriptions/checkout"
                                    ) {
                                        "El enlace de pago no es válido."
                                    }

                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, uri)
                                            .addCategory(Intent.CATEGORY_BROWSABLE)
                                    )
                                } catch (e: CancellationException) {
                                    throw e
                                } catch (_: ActivityNotFoundException) {
                                    snackbarHostState.showSnackbar(
                                        "No hay un navegador disponible."
                                    )
                                } catch (e: FirebaseFunctionsException) {
                                    if (auth.currentUser?.uid == uid) {
                                        val message = if (
                                            e.code == FirebaseFunctionsException.Code.FAILED_PRECONDITION
                                        ) {
                                            e.message ?: "No se pudo iniciar la compra."
                                        } else {
                                            "No pudimos preparar el pago. Intenta de nuevo en un momento."
                                        }
                                        snackbarHostState.showSnackbar(message)
                                    }
                                } catch (e: IllegalStateException) {
                                    snackbarHostState.showSnackbar(
                                        e.message ?: "No se pudo iniciar la compra."
                                    )
                                } catch (_: Exception) {
                                    snackbarHostState.showSnackbar("No se pudo abrir Mercado Pago.")
                                } finally {
                                    creatingCheckout = false
                                }
                            }
                        }
                    )
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun PlanCard(
    plan: PlanUiModel,
    actionEnabled: Boolean,
    actionLabel: String,
    showFreeAction: Boolean,
    unavailableLabel: String,
    onSelect: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    val buttonShape = RoundedCornerShape(12.dp)
    val highlighted = plan.isPopular
    val cardModifier = Modifier.fillMaxWidth().then(
        if (highlighted) Modifier.border(2.dp, PurpleBrush, shape) else Modifier
    )

    Card(
        modifier = cardModifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = shape,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (highlighted) 8.dp else 2.dp
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            if (highlighted) {
                Text(
                    "MÁS POPULAR",
                    color = GradientPurpleStart,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Text(plan.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DarkBlue)
            Spacer(Modifier.height(4.dp))
            Text(plan.description, fontSize = 14.sp, color = Color.Gray, lineHeight = 18.sp)
            Spacer(Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    plan.price,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DarkBlue
                )
                Text(
                    " ${plan.period}",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            val disabledLabel = when {
                plan.isCurrent -> "Plan actual"
                plan.amountClp == 0L && !showFreeAction -> "Plan sin costo"
                !actionEnabled -> unavailableLabel
                else -> null
            }

            if (disabledLabel != null) {
                OutlinedButton(
                    onClick = {},
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                    shape = buttonShape
                ) {
                    Text(disabledLabel)
                }
            } else {
                Button(
                    onClick = onSelect,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    contentPadding = PaddingValues(0.dp),
                    shape = buttonShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (highlighted) Color.Transparent else DarkBlue
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                            .then(
                                if (highlighted) Modifier.background(PurpleBrush, buttonShape)
                                else Modifier
                            )
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            actionLabel,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            HorizontalDivider(color = Color(0xFFE2E8F0))
            Spacer(Modifier.height(16.dp))

            plan.features.forEach { feature ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = if (highlighted) GradientPurpleStart else DarkBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(feature, fontSize = 14.sp, color = Color(0xFF334155), lineHeight = 18.sp)
                }
            }
        }
    }
}

@Composable
fun LimitReachedDialog(
    fechaReinicio: String,
    customMessage: String? = null,
    onDismiss: () -> Unit,
    onUpgrade: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(64.dp).background(Color(0xFFFEF2F2), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.WarningAmber,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))
                Text(
                    "Límite alcanzado",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkBlue
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    customMessage
                        ?: "Has utilizado toda la cuota de esta función incluida en tu plan Gratis. " +
                        "Tu cuota se reiniciará el $fechaReinicio.",
                    fontSize = 15.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = onUpgrade,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize()
                            .background(PurpleBrush, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Mejorar plan", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                Spacer(Modifier.height(12.dp))
                TextButton(onClick = onDismiss) { Text("Cerrar", color = Color.Gray) }
            }
        }
    }
}