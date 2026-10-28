package com.nerdsyntax.juntalucas.feature.movements.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nerdsyntax.juntalucas.feature.movements.domain.PaymentMethod
import com.nerdsyntax.juntalucas.feature.movements.domain.SaleDates

private val DarkBlue = Color(0xFF0F2A4A)
private val LightBg = Color(0xFFF8FAFC)
private val TextGray = Color(0xFF475569)
private val BorderLight = Color(0xFFE2E8F0)

private val GreenPos = Color(0xFF15803D)
private val GreenSoftBg = Color(0xFFF0FDF4)
private val RedNeg = Color(0xFFB91C1C)
private val RedSoftBg = Color(0xFFFEF2F2)

private val GradientPurpleStart = Color(0xFF6366F1)
private val GradientPurpleEnd = Color(0xFF9333EA)
private val BrandPurple = Color(0xFF7C3AED)
private val LightPurple = Color(0xFFF3E8FF)

private val PrimaryPurpleGradient = Brush.horizontalGradient(
    colors = listOf(GradientPurpleStart, GradientPurpleEnd)
)

@Composable
fun MovementsScreen(
    state: MovementsUiState,
    onTabSelected: (MovementTab) -> Unit = {},
    onSearchChange: (String) -> Unit = {},
    onFilterSelected: (String) -> Unit = {},
    onAddClick: (Boolean) -> Unit = {},
    onRetry: () -> Unit = {},
    onChangeMonth: (Int) -> Unit = {}
) {
    val isSalesTab = state.selectedTab == MovementTab.VENTAS
    val currentMonthLabel = SaleDates.monthLabel(state.month).replaceFirstChar { it.uppercase() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = DarkBlue,
                        shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
                    )
                    .padding(top = 48.dp, bottom = 24.dp, start = 20.dp, end = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "REGISTRO FINANCIERO",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Movimientos",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.White.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            IconButton(
                                onClick = { onChangeMonth(-1) },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronLeft,
                                    contentDescription = "Mes anterior",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = currentMonthLabel,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 6.dp)
                            )
                            IconButton(
                                onClick = { onChangeMonth(1) },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Mes siguiente",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.1f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SegmentedTabItem(
                            modifier = Modifier.weight(1f),
                            text = "Ventas",
                            isSelected = isSalesTab,
                            onClick = { onTabSelected(MovementTab.VENTAS) }
                        )
                        SegmentedTabItem(
                            modifier = Modifier.weight(1f),
                            text = "Gastos",
                            isSelected = !isSalesTab,
                            onClick = { onTabSelected(MovementTab.GASTOS) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color.White.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                        modifier = Modifier.weight(1.25f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = if (isSalesTab) "Total vendido" else "Total gastado",
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isSalesTab) state.totalVendido else state.totalGastos,
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color.White.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = if (isSalesTab) "Ticket promedio" else "Gasto promedio",
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = state.ticketPromedio,
                                color = Color.White,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(PrimaryPurpleGradient)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Column(modifier = Modifier.padding(horizontal = 18.dp)) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White,
                    shadowElevation = 1.dp,
                    border = BorderStroke(1.dp, BorderLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = onSearchChange,
                        placeholder = {
                            Text(
                                text = if (isSalesTab) "Buscar venta por nombre..." else "Buscar gasto por descripción...",
                                color = TextGray.copy(alpha = 0.6f),
                                fontSize = 14.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = BrandPurple
                            )
                        },
                        trailingIcon = {
                            if (state.searchQuery.isNotBlank()) {
                                IconButton(onClick = { onSearchChange("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Limpiar",
                                        tint = TextGray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White,
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = BrandPurple
                        ),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filters = listOf("Todos") + PaymentMethod.entries.map { it.label }
                    filters.forEach { filter ->
                        val isSelected = state.selectedFilter == filter
                        Surface(
                            onClick = { onFilterSelected(filter) },
                            shape = RoundedCornerShape(50),
                            color = if (isSelected) DarkBlue else Color.White,
                            border = BorderStroke(1.dp, if (isSelected) DarkBlue else BorderLight),
                            shadowElevation = if (isSelected) 1.dp else 0.dp
                        ) {
                            Text(
                                text = filter,
                                color = if (isSelected) Color.White else TextGray,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (state.fromCache && state.errorMessage == null) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = LightPurple,
                        border = BorderStroke(1.dp, BrandPurple.copy(alpha = 0.25f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Info,
                                    contentDescription = null,
                                    tint = BrandPurple,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Datos locales pendientes de sincronizar.",
                                    color = DarkBlue,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            TextButton(onClick = onRetry) {
                                Text("Actualizar", color = BrandPurple, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shape = RoundedCornerShape(20.dp),
                    shadowElevation = 1.dp,
                    border = BorderStroke(0.5.dp, BorderLight)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        when {
                            state.isLoading -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator(color = BrandPurple, strokeWidth = 3.dp)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = if (isSalesTab) "Cargando ventas..." else "Cargando gastos...",
                                        color = TextGray,
                                        fontSize = 14.sp
                                    )
                                }
                            }

                            state.errorMessage != null -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = state.errorMessage,
                                        color = RedNeg,
                                        fontSize = 14.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedButton(
                                        onClick = onRetry,
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Reintentar")
                                    }
                                }
                            }

                            state.movements.isEmpty() -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 32.dp, horizontal = 16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(CircleShape)
                                            .background(LightBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.ReceiptLong,
                                            contentDescription = null,
                                            tint = TextGray.copy(alpha = 0.6f),
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = if (state.searchQuery.isNotBlank() || state.selectedFilter != "Todos") {
                                            if (isSalesTab) "Sin resultados para tu búsqueda de ventas"
                                            else "Sin resultados para tu búsqueda de gastos"
                                        } else {
                                            if (isSalesTab) "Aún no registras ventas este mes"
                                            else "Aún no registras gastos este mes"
                                        },
                                        color = DarkBlue,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (isSalesTab) "Usa el botón '+ Nueva venta' para comenzar."
                                        else "Usa el botón '+ Nuevo gasto' para registrar tus egresos.",
                                        color = TextGray,
                                        fontSize = 13.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            else -> {
                                state.movements.forEachIndexed { index, mov ->
                                    val iconColor = if (mov.isIncome) GreenPos else RedNeg
                                    val bgColor = if (mov.isIncome) GreenSoftBg else RedSoftBg

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(bgColor),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (mov.isIncome) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                                contentDescription = null,
                                                tint = iconColor,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(14.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = mov.title,
                                                fontWeight = FontWeight.Bold,
                                                color = DarkBlue,
                                                fontSize = 15.sp
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = mov.date,
                                                    color = TextGray,
                                                    fontSize = 12.sp
                                                )
                                                Text(
                                                    text = " · ",
                                                    color = TextGray,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = LightBg
                                                ) {
                                                    Text(
                                                        text = mov.paymentMethod,
                                                        color = DarkBlue,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Text(
                                            text = mov.amount,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = iconColor,
                                            fontSize = 16.sp
                                        )
                                    }

                                    if (index < state.movements.lastIndex) {
                                        HorizontalDivider(color = Color(0xFFF1F5F9))
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 24.dp)
        ) {
            Surface(
                onClick = { onAddClick(isSalesTab) },
                shape = RoundedCornerShape(50),
                shadowElevation = 6.dp,
                color = Color.Transparent
            ) {
                Row(
                    modifier = Modifier
                        .background(PrimaryPurpleGradient)
                        .padding(horizontal = 22.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isSalesTab) "Nueva venta" else "Nuevo gasto",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SegmentedTabItem(
    modifier: Modifier = Modifier,
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) Color.White else Color.Transparent,
        shadowElevation = if (isSelected) 2.dp else 0.dp,
        modifier = modifier.height(40.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                color = if (isSelected) DarkBlue else Color.White.copy(alpha = 0.75f),
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 14.sp
            )
        }
    }
}