package com.nerdsyntax.juntalucas.feature.business.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

private val DarkBlue = Color(0xFF0F2A4A)
private val HeaderGradientEnd = Color(0xFF2E1A68)
private val LightBg = Color(0xFFF8FAFC)
private val PurpleAi = Color(0xFF8B5CF6)
private val LightPurple = Color(0xFFEDE9FE)
private val GradientStart = Color(0xFF6366F1)
private val GradientEnd = Color(0xFF9333EA)
private val TextGray = Color(0xFF475569)
private val TextMuted = Color(0xFF94A3B8)
private val BorderGray = Color(0xFFE2E8F0)
private val DividerGray = Color(0xFFF1F5F9)
private val GreenText = Color(0xFF15803D)
private val GreenBg = Color(0xFFF0FDF4)
private val RedText = Color(0xFFB91C1C)
private val RedBg = Color(0xFFFEF2F2)
private val BlueText = Color(0xFF1D4ED8)
private val BlueBg = Color(0xFFEFF6FF)
private val PurpleText = Color(0xFF6D28D9)
private val PurpleBg = Color(0xFFF5F3FF)
private val SlateBg = Color(0xFFF1F5F9)

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
    onBusinessComunaChange: (String) -> Unit = {},
    onBusinessMetaChange: (String) -> Unit = {}
) {
    Box(modifier = Modifier.fillMaxSize().background(LightBg)) {
        Column(modifier = Modifier.fillMaxSize()) {

            BusinessHeader(
                selectedTab = state.selectedTab,
                onTabSelected = onTabSelected
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                if (state.selectedTab == BusinessTab.PRODUCTOS) {
                    ProductsTabContent(
                        state = state,
                        onSearchChange = onSearchChange,
                        onFilterSelected = onFilterSelected,
                        onEditProduct = onEditProduct,
                        onDeactivateProduct = onDeactivateProduct,
                        onRetry = onRetry
                    )
                } else {
                    InfoTabContent(
                        state = state,
                        onRetry = onRetry,
                        onStartEditing = onStartEditingBusiness,
                        onCancelEditing = onCancelEditingBusiness,
                        onSave = onSaveBusiness,
                        onNameChange = onBusinessNameChange,
                        onRubroChange = onBusinessRubroChange,
                        onRegionChange = onBusinessRegionChange,
                        onComunaChange = onBusinessComunaChange,
                        onMetaChange = onBusinessMetaChange
                    )
                }
                Spacer(modifier = Modifier.height(90.dp))
            }
        }

        if (state.selectedTab == BusinessTab.PRODUCTOS) {
            GradientButton(
                text = "Agregar",
                icon = Icons.Default.Add,
                onClick = onAddClick,
                elevation = 3.dp,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
            )
        }
    }
}

@Composable
private fun BusinessHeader(
    selectedTab: BusinessTab,
    onTabSelected: (BusinessTab) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkBlue, RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
    ) {
        Text("Mi Negocio", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Administra tu catálogo y la información de tu negocio",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 13.sp
        )
        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            TabPill(
                text = "Productos y servicios",
                isSelected = selectedTab == BusinessTab.PRODUCTOS,
                modifier = Modifier.weight(1f),
                onClick = { onTabSelected(BusinessTab.PRODUCTOS) }
            )
            TabPill(
                text = "Información",
                isSelected = selectedTab == BusinessTab.INFORMACION,
                modifier = Modifier.weight(1f),
                onClick = { onTabSelected(BusinessTab.INFORMACION) }
            )
        }
    }
}

@Composable
private fun TabPill(
    text: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color.White.copy(alpha = 0.2f) else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 14.sp
        )
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
            placeholder = { Text("Buscar productos o servicios...", color = TextMuted) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedTextColor = DarkBlue,
                unfocusedTextColor = DarkBlue,
                disabledTextColor = TextMuted,
                cursorColor = PurpleAi,
                focusedContainerColor = Color.White,
                unfocusedBorderColor = BorderGray,
                focusedBorderColor = PurpleAi
            ),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Todos", "Productos", "Servicios", "Activos").forEach { filter ->
                FilterPill(
                    text = filter,
                    isSelected = state.selectedFilter == filter,
                    onClick = { onFilterSelected(filter) }
                )
            }
        }
        Spacer(modifier = Modifier.height(18.dp))

        if (state.isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)),
                color = PurpleAi,
                trackColor = LightPurple
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        state.errorMessage?.let { message ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = RedBg),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(message, color = RedText, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    TextButton(onClick = onRetry, contentPadding = PaddingValues(0.dp)) {
                        Text("Reintentar", color = PurpleText, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        if (!state.isLoading && state.errorMessage == null && state.products.isEmpty()) {
            EmptyCatalogCard()
        }

        state.products.forEach { product ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .alpha(if (product.active) 1f else 0.65f),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = product.name,
                                fontWeight = FontWeight.Bold,
                                color = DarkBlue,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TypeBadge(product.type)
                                if (!product.active) {
                                    StatusBadge("Inactivo")
                                }
                            }
                        }

                        ProductActionsMenu(
                            isActive = product.active,
                            onEdit = { onEditProduct(product.id) },
                            onToggleActive = { onDeactivateProduct(product.id) }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricTile("Precio venta", product.price, GreenText, GreenBg, Modifier.weight(1f))
                        MetricTile("Costo", product.cost, TextGray, SlateBg, Modifier.weight(1f))
                        MetricTile("Margen", product.margin, PurpleText, PurpleBg, Modifier.weight(1f))
                        MetricTile("Stock", product.stock, BlueText, BlueBg, Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterPill(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(50)
    Box(
        modifier = Modifier
            .clip(shape)
            .background(if (isSelected) LightPurple else Color.White)
            .border(1.dp, if (isSelected) PurpleAi else BorderGray, shape)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = if (isSelected) PurpleText else TextGray,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun ProductActionsMenu(
    isActive: Boolean,
    onEdit: () -> Unit,
    onToggleActive: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.MoreVert, contentDescription = "Más opciones", tint = TextMuted)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("Editar") },
                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = TextGray) },
                onClick = {
                    expanded = false
                    onEdit()
                }
            )
            DropdownMenuItem(
                text = { Text(if (isActive) "Desactivar" else "Activar") },
                leadingIcon = {
                    Icon(
                        imageVector = if (isActive) Icons.Default.Block else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (isActive) RedText else GreenText
                    )
                },
                onClick = {
                    expanded = false
                    onToggleActive()
                }
            )
        }
    }
}

@Composable
private fun TypeBadge(type: String) {
    val esServicio = type.contains("serv", ignoreCase = true)
    val fondo = if (esServicio) PurpleBg else BlueBg
    val texto = if (esServicio) PurpleText else BlueText
    Box(
        modifier = Modifier
            .background(fondo, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(type, color = texto, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun StatusBadge(text: String) {
    Box(
        modifier = Modifier
            .background(SlateBg, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(text, color = TextGray, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun MetricTile(
    label: String,
    value: String,
    color: Color,
    background: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(background, RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 10.dp)
    ) {
        Text(label, color = TextGray, fontSize = 10.sp, maxLines = 1)
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = value,
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
private fun EmptyCatalogCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(56.dp).background(LightPurple, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Inventory2, contentDescription = null, tint = PurpleAi, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text("Tu catálogo está vacío", color = DarkBlue, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Agrega tus productos o servicios para registrar ventas y ver tu margen.",
                color = TextGray,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun InfoTabContent(
    state: BusinessUiState,
    onRetry: () -> Unit,
    onStartEditing: () -> Unit,
    onCancelEditing: () -> Unit,
    onSave: () -> Unit,
    onNameChange: (String) -> Unit,
    onRubroChange: (String) -> Unit,
    onRegionChange: (String) -> Unit,
    onComunaChange: (String) -> Unit,
    onMetaChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Brush.horizontalGradient(listOf(DarkBlue, HeaderGradientEnd)))
                .padding(20.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Store, contentDescription = null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(state.businessName, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(state.businessDetails, color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    HeroStat(state.productCount, "Productos", Modifier.weight(1f))
                    HeroStat(state.serviceCount, "Servicios", Modifier.weight(1f))
                    HeroStat(state.assetCount, "Activos", Modifier.weight(1f))
                }
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Meta mensual de ventas", color = TextGray, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Box(
                        modifier = Modifier
                            .background(LightPurple, RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${(state.goalPercentage * 100).toInt()}%",
                            color = PurpleText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(state.goalTotal, color = DarkBlue, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(modifier = Modifier.height(14.dp))
                LinearProgressIndicator(
                    progress = { state.goalPercentage },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = PurpleAi,
                    trackColor = BorderGray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(state.goalProgressText, color = TextMuted, fontSize = 12.sp)
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Información del negocio", color = DarkBlue, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(14.dp))

                if (state.isEditingBusiness) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        BusinessTextField(state.businessName, onNameChange, "Nombre")
                        BusinessTextField(state.rubro, onRubroChange, "Rubro")
                        BusinessTextField(state.region, onRegionChange, "Región")
                        BusinessTextField(state.comuna, onComunaChange, "Comuna")
                        BusinessTextField(
                            value = state.metaMensual,
                            onValueChange = onMetaChange,
                            label = "Meta mensual de ventas (CLP)",
                            isNumber = true,
                            prefix = "$",
                            supportingText = metaPreview(state.metaMensual)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                } else {
                    InfoDetailRow(Icons.Default.Folder, "Rubro", state.rubro.ifBlank { "—" })
                    HorizontalDivider(color = DividerGray)
                    InfoDetailRow(Icons.Default.Work, "Actividad", state.actividad.ifBlank { "—" })
                    HorizontalDivider(color = DividerGray)
                    InfoDetailRow(Icons.Default.Place, "Región", state.region.ifBlank { "—" })
                    HorizontalDivider(color = DividerGray)
                    InfoDetailRow(Icons.Default.LocationCity, "Comuna", state.comuna.ifBlank { "—" })
                    HorizontalDivider(color = DividerGray)
                }
                InfoDetailRow(Icons.Default.Payments, "Moneda", state.moneda)
                HorizontalDivider(color = DividerGray)
                InfoDetailRow(Icons.Default.CalendarMonth, "Registrado desde", state.registro)
            }
        }
        state.errorMessage?.let { message ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = RedBg),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(message, color = RedText, fontSize = 14.sp)
                    if (!state.isEditingBusiness) {
                        Spacer(modifier = Modifier.height(4.dp))
                        TextButton(onClick = onRetry, contentPadding = PaddingValues(0.dp)) {
                            Text("Reintentar", color = PurpleText, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        if (state.isEditingBusiness) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCancelEditing,
                    enabled = !state.isSavingBusiness,
                    modifier = Modifier.weight(1f).height(54.dp),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, BorderGray),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextGray)
                ) {
                    Text("Cancelar", fontWeight = FontWeight.Bold)
                }
                GradientButton(
                    text = if (state.isSavingBusiness) "Guardando..." else "Guardar",
                    onClick = onSave,
                    enabled = !state.isSavingBusiness,
                    modifier = Modifier.weight(1f).height(54.dp)
                )
            }
        } else {
            GradientButton(
                text = "Editar información del negocio",
                icon = Icons.Default.Edit,
                onClick = onStartEditing,
                modifier = Modifier.fillMaxWidth().height(54.dp)
            )
        }
    }
}

@Composable
private fun BusinessTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isNumber: Boolean = false,
    prefix: String? = null,
    supportingText: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        prefix = prefix?.let { { Text(it, color = TextGray) } },
        supportingText = supportingText?.let { { Text(it, color = TextMuted) } },
        keyboardOptions = if (isNumber) KeyboardOptions(keyboardType = KeyboardType.Number) else KeyboardOptions.Default,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = Color.White,
            focusedTextColor = DarkBlue,
            unfocusedTextColor = DarkBlue,
            disabledTextColor = TextMuted,
            cursorColor = PurpleAi,
            focusedContainerColor = Color.White,
            unfocusedBorderColor = BorderGray,
            focusedBorderColor = PurpleAi,
            focusedLabelColor = PurpleAi
        )
    )
}

/** Muestra el monto con puntos de miles mientras se escribe, p. ej. 2500000 -> $2.500.000. */
private fun metaPreview(digits: String): String {
    val monto = digits.toLongOrNull()
    if (monto == null || monto <= 0L) return "Déjalo vacío si no quieres definir una meta"
    val symbols = DecimalFormatSymbols().apply { groupingSeparator = '.' }
    return "$" + DecimalFormat("#,###", symbols).format(monto)
}

@Composable
private fun HeroStat(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
            .padding(vertical = 12.dp)
    ) {
        Text(value, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
    }
}

@Composable
private fun InfoDetailRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(32.dp).background(LightPurple, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = PurpleAi, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, color = TextGray, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Text(value, color = DarkBlue, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}
@Composable
private fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    elevation: Dp = 0.dp
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent,
        shadowElevation = elevation
    ) {
        Row(
            modifier = Modifier
                .alpha(if (enabled) 1f else 0.55f)
                .background(Brush.horizontalGradient(listOf(GradientStart, GradientEnd)))
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}