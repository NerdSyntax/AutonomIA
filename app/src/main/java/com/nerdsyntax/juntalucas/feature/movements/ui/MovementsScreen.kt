package com.nerdsyntax.juntalucas.feature.movements.ui

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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DarkBlue = Color(0xFF0F2A4A)
private val LightBg = Color(0xFFF8FAFC)
private val GreenPos = Color(0xFF10B981)
private val RedNeg = Color(0xFFEF4444)
private val PurpleLine = Color(0xFF8B5CF6)

@Composable
fun MovementsScreen(
    state: MovementsUiState,
    onTabSelected: (MovementTab) -> Unit = {},
    onSearchChange: (String) -> Unit = {},
    onFilterSelected: (String) -> Unit = {},
    onAddClick: (Boolean) -> Unit = {}
) {
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
                        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                    )
                    .padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
            ) {
                Text("Movimientos", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(16.dp))

                // Pestaña de ventas y gastos
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    TabButton(
                        text = "Ventas",
                        isSelected = state.selectedTab == MovementTab.VENTAS,
                        onClick = { onTabSelected(MovementTab.VENTAS) }
                    )
                    TabButton(
                        text = "Gastos",
                        isSelected = state.selectedTab == MovementTab.GASTOS,
                        onClick = { onTabSelected(MovementTab.GASTOS) }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (state.selectedTab == MovementTab.VENTAS) "Total vendido en septiembre" else "Total gastado en septiembre",
                            color = Color.LightGray,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (state.selectedTab == MovementTab.VENTAS) state.totalVendido else state.totalGastos,
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Ticket promedio", color = Color.LightGray, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(state.ticketPromedio, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(
                            brush = Brush.horizontalGradient(listOf(PurpleLine, Color(0xFF4C1D95))),
                            shape = RoundedCornerShape(1.5.dp)
                        )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))


            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { Text(if (state.selectedTab == MovementTab.VENTAS) "Buscar venta..." else "Buscar gasto...", color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White,
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedBorderColor = DarkBlue
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filters = listOf("Todos", "Efectivo", "Débito", "Crédito", "Transferencia")
                    filters.forEach { filter ->
                        FilterChip(
                            selected = state.selectedFilter == filter,
                            onClick = { onFilterSelected(filter) },
                            label = { Text(filter) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DarkBlue,
                                selectedLabelColor = Color.White,
                                containerColor = Color.White,
                                labelColor = Color.DarkGray
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = state.selectedFilter == filter,
                                borderColor = Color(0xFFE2E8F0)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        state.movements.forEach { mov ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val iconColor = if (mov.isIncome) GreenPos else RedNeg
                                val bgColor = if (mov.isIncome) Color(0xFFD1FAE5) else Color(0xFFFEE2E2)

                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(bgColor, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (mov.isIncome) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                        contentDescription = null,
                                        tint = iconColor
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(mov.title, fontWeight = FontWeight.SemiBold, color = DarkBlue, fontSize = 14.sp)
                                    Text("${mov.date} · ${mov.paymentMethod}", color = Color.Gray, fontSize = 12.sp)
                                }

                                Text(mov.amount, fontWeight = FontWeight.Bold, color = iconColor, fontSize = 15.sp)
                            }
                            if (mov != state.movements.last()) {
                                HorizontalDivider(color = Color(0xFFF1F5F9))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Button(
                onClick = { onAddClick(state.selectedTab == MovementTab.VENTAS) },
                colors = ButtonDefaults.buttonColors(containerColor = DarkBlue),
                shape = RoundedCornerShape(24.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (state.selectedTab == MovementTab.VENTAS) "Nueva venta" else "Nuevo gasto",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
private fun TabButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else Color.Gray,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .width(50.dp)
                .height(3.dp)
                .background(if (isSelected) Color.White else Color.Transparent, RoundedCornerShape(1.5.dp))
        )
    }
}