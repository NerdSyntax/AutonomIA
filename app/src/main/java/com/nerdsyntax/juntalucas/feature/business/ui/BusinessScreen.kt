package com.nerdsyntax.juntalucas.feature.business.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DarkBlue = Color(0xFF0F2A4A)
private val LightBg = Color(0xFFF8FAFC)
private val PurpleLine = Color(0xFF8B5CF6)

@Composable
fun BusinessScreen(
    state: BusinessUiState,
    onTabSelected: (BusinessTab) -> Unit = {},
    onSearchChange: (String) -> Unit = {},
    onFilterSelected: (String) -> Unit = {},
    onAddClick: () -> Unit = {},
    onEditProduct: (String) -> Unit = {},
    onDeactivateProduct: (String) -> Unit = {},
    onRetry: () -> Unit = {},
    onStartEditingBusiness: () -> Unit = {},
    onCancelEditingBusiness: () -> Unit = {},
    onSaveBusiness: () -> Unit = {},
    onBusinessNameChange: (String) -> Unit = {},
    onBusinessRubroChange: (String) -> Unit = {},
    onBusinessRegionChange: (String) -> Unit = {},
    onBusinessComunaChange: (String) -> Unit = {}
) {
    Box(modifier = Modifier.fillMaxSize().background(LightBg)) {
        Column(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkBlue, RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    .padding(top = 48.dp, bottom = 0.dp, start = 24.dp, end = 24.dp)
            ) {
                Text("Mi Negocio", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    TabButton(
                        text = "Productos y servicios",
                        isSelected = state.selectedTab == BusinessTab.PRODUCTOS,
                        onClick = { onTabSelected(BusinessTab.PRODUCTOS) }
                    )
                    TabButton(
                        text = "Información",
                        isSelected = state.selectedTab == BusinessTab.INFORMACION,
                        onClick = { onTabSelected(BusinessTab.INFORMACION) }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                if (state.selectedTab == BusinessTab.PRODUCTOS) {
                    ProductsTabContent(state, onSearchChange, onFilterSelected, onEditProduct, onDeactivateProduct, onRetry)
                } else {
                    InfoTabContent(state, onStartEditingBusiness, onCancelEditingBusiness, onSaveBusiness,
                        onBusinessNameChange, onBusinessRubroChange, onBusinessRegionChange, onBusinessComunaChange)
                }
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        if (state.selectedTab == BusinessTab.PRODUCTOS) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
            ) {
                Button(
                    onClick = onAddClick,
                    colors = ButtonDefaults.buttonColors(containerColor = DarkBlue),
                    shape = RoundedCornerShape(24.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Agregar", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
private fun ProductsTabContent(
    state: BusinessUiState,
    onSearchChange: (String) -> Unit,
    onFilterSelected: (String) -> Unit,
    onEditProduct: (String) -> Unit,
    onDeactivateProduct: (String) -> Unit,
    onRetry: () -> Unit
) {
    Column {
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Buscar productos o servicios...", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                unfocusedBorderColor = Color(0xFFE2E8F0),
                focusedBorderColor = DarkBlue
            ),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filters = listOf("Todos", "Productos", "Servicios", "Activos")
            filters.forEach { filter ->
                val isSelected = state.selectedFilter == filter
                FilterChip(
                    selected = isSelected,
                    onClick = { onFilterSelected(filter) },
                    label = {
                        Text(
                            text = filter,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = DarkBlue,
                        selectedLabelColor = Color.White,
                        containerColor = Color.White,
                        labelColor = Color.DarkGray
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = Color(0xFFE2E8F0),
                        selectedBorderColor = DarkBlue
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))

        if (state.isLoading) LinearProgressIndicator(Modifier.fillMaxWidth())
        state.errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
            TextButton(onClick = onRetry) { Text("Reintentar") }
        }
        if (!state.isLoading && state.errorMessage == null && state.products.isEmpty()) {
            Text("No hay productos o servicios en tu catálogo.", color = Color.Gray)
        }

        state.products.forEach { product ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFF1F5F9))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(product.name, fontWeight = FontWeight.Bold, color = DarkBlue, fontSize = 16.sp)
                        Row {
                            TextButton(onClick = { onEditProduct(product.id) }) { Text("Editar") }
                            TextButton(onClick = { onDeactivateProduct(product.id) }) { Text(if (product.active) "Desactivar" else "Activar") }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))

                    Box(modifier = Modifier.background(Color(0xFFEFF6FF), RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                        Text(product.type, color = Color(0xFF2563EB), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        InfoColumn("Precio venta", product.price)
                        InfoColumn("Costo", product.cost)
                        InfoColumn("Margen", product.margin)
                        InfoColumn("Stock", product.stock)
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoTabContent(
    state: BusinessUiState,
    onStartEditing: () -> Unit,
    onCancelEditing: () -> Unit,
    onSave: () -> Unit,
    onNameChange: (String) -> Unit,
    onRubroChange: (String) -> Unit,
    onRegionChange: (String) -> Unit,
    onComunaChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkBlue),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(state.businessName, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(state.businessDetails, color = Color.LightGray, fontSize = 13.sp)

                Spacer(modifier = Modifier.height(20.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    HeroStat(state.productCount, "Productos", Modifier.weight(1f))
                    HeroStat(state.serviceCount, "Servicios", Modifier.weight(1f))
                    HeroStat(state.assetCount, "Activos", Modifier.weight(1f))
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFFF1F5F9))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Meta mensual de ventas", color = DarkBlue, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(state.goalTotal, color = DarkBlue, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = { state.goalPercentage },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = PurpleLine,
                    trackColor = Color(0xFFF1F5F9),
                    strokeCap = StrokeCap.Round
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(state.goalProgressText, color = Color.Gray, fontSize = 12.sp)
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFFF1F5F9))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Información del negocio", color = DarkBlue, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                if (state.isEditingBusiness) {
                    OutlinedTextField(state.businessName, onNameChange, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(state.rubro, onRubroChange, label = { Text("Rubro") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(state.region, onRegionChange, label = { Text("Región") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(state.comuna, onComunaChange, label = { Text("Comuna") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                } else {
                    InfoDetailRow("Rubro", state.rubro.ifBlank { "—" })
                    InfoDetailRow("Actividad", state.actividad.ifBlank { "—" })
                    InfoDetailRow("Región", state.region.ifBlank { "—" })
                    InfoDetailRow("Comuna", state.comuna.ifBlank { "—" })
                }
                InfoDetailRow("Moneda", state.moneda)
                InfoDetailRow("Registrado desde", state.registro)
            }
        }

        if (state.isEditingBusiness) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onCancelEditing, enabled = !state.isSavingBusiness, modifier = Modifier.weight(1f).height(54.dp)) { Text("Cancelar") }
                Button(onClick = onSave, enabled = !state.isSavingBusiness, modifier = Modifier.weight(1f).height(54.dp)) { Text(if (state.isSavingBusiness) "Guardando..." else "Guardar") }
            }
        } else OutlinedButton(
            onClick = onStartEditing,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, DarkBlue),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkBlue)
        ) {
            Text("Editar información del negocio", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}


@Composable
private fun TabButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier.clickable { onClick() }.padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else Color.Gray,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 15.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(modifier = Modifier.width(40.dp).height(3.dp).background(if (isSelected) Color.White else Color.Transparent, RoundedCornerShape(1.5.dp)))
    }
}

@Composable
private fun InfoColumn(label: String, value: String) {
    Column {
        Text(label, color = Color.Gray, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(value, color = DarkBlue, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun HeroStat(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .padding(vertical = 12.dp)
    ) {
        Text(value, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
    }
}

@Composable
private fun InfoDetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.Gray, fontSize = 14.sp)
        Text(value, color = DarkBlue, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}
