package com.nerdsyntax.juntalucas.feature.business.ui.product

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.media.ExifInterface
import android.os.Build
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.produceState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import kotlin.math.abs
import com.nerdsyntax.juntalucas.feature.paywall.ui.LimitReachedDialog
import com.nerdsyntax.juntalucas.feature.paywall.ui.PaywallScreen
import androidx.compose.ui.window.Dialog

private val DarkBlue = Color(0xFF0F2A4A)
private val LightBg = Color(0xFFF8FAFC)
private val PurpleAi = Color(0xFF8B5CF6)
private val GradientStart = Color(0xFF6366F1)
private val GradientEnd = Color(0xFF9333EA)
private val TextGray = Color(0xFF475569)
private val TextMuted = Color(0xFF94A3B8)
private val BorderGray = Color(0xFFE2E8F0)
private val GreenText = Color(0xFF15803D)
private val GreenBg = Color(0xFFF0FDF4)
private val RedText = Color(0xFFB91C1C)
private val RedBg = Color(0xFFFEF2F2)
private val PurpleText = Color(0xFF6D28D9)
private val PurpleBg = Color(0xFFF5F3FF)

@Suppress("UNUSED_PARAMETER")
@Composable
fun AddProductScreen(
    state: AddProductUiState,
    vm: AddProductViewModel,
    onIsProductChange: (Boolean) -> Unit,
    onNombreChange: (String) -> Unit,
    onCategoriaChange: (String) -> Unit,
    onPrecioChange: (String) -> Unit,
    onCostoChange: (String) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onStockActualChange: (String) -> Unit,
    onStockMinimoChange: (String) -> Unit,
    onUnidadChange: (String) -> Unit = {},
    onIsActiveChange: (Boolean) -> Unit,
    onSave: () -> Unit,
    onPhotoChange: (String?) -> Unit,
    onNavigateBack: () -> Unit
) {
    val symbols = DecimalFormatSymbols().apply { groupingSeparator = '.' }
    val formatter = DecimalFormat("#,###", symbols)

    val gananciaStr = (if (state.ganancia < 0) "-" else "") + "$" + formatter.format(abs(state.ganancia))
    val margenStr = "${state.margen.toInt()}%"
    var mostrarPantallaPlanes by rememberSaveable { mutableStateOf(false) }

    if (state.mostrarPaywallSuscripcion) {
        LimitReachedDialog(
            fechaReinicio = "próximo ciclo mensual",
            onDismiss = {
                vm.ocultarPaywallSuscripcion()
            },
            onUpgrade = {
                vm.ocultarPaywallSuscripcion()
                mostrarPantallaPlanes = true
            }
        )
    }

    if (mostrarPantallaPlanes) {
        Dialog(
            onDismissRequest = { mostrarPantallaPlanes = false },
            properties = androidx.compose.ui.window.DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            PaywallScreen(
                onClose = { mostrarPantallaPlanes = false },
                onSelectPlan = { planId ->
                    mostrarPantallaPlanes = false
                }
            )
        }
    }
    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkBlue, RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    .padding(top = 48.dp, bottom = 22.dp, start = 8.dp, end = 24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = "Agregar producto o servicio",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Completa los datos para tu catálogo",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 13.sp
                        )
                    }
                }
            }
        },
        containerColor = LightBg
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderGray),
                color = Color.White
            ) {
                Row(
                    modifier = Modifier.padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TypePill("Producto", Icons.Default.Inventory, state.isProduct, Modifier.weight(1f)) { onIsProductChange(true) }
                    TypePill("Servicio", Icons.Default.Settings, !state.isProduct, Modifier.weight(1f)) { onIsProductChange(false) }
                }
            }

            ProductPhotoCard(
                isProduct = state.isProduct,
                enabled = !state.isSaving,
                selectedPhoto = state.selectedPhotoUri,
                storedPhotoPath = state.storedPhotoPath.takeUnless { state.removeStoredPhoto },
                onPhotoChange = onPhotoChange
            )

            FormCard(title = "Información básica") {
                ProductField(
                    label = "Nombre *",
                    value = state.nombre,
                    placeholder = "Ej: Cupcakes (docena)",
                    enabled = !state.isSaving,
                    onValueChange = onNombreChange
                )
                ProductField(
                    label = "Categoría",
                    value = state.categoria,
                    placeholder = "Ej: Pastelería",
                    enabled = !state.isSaving,
                    onValueChange = onCategoriaChange
                )
                ProductField(
                    label = "Descripción (opcional)",
                    value = state.descripcion,
                    placeholder = if (state.isProduct) "Características del producto..." else "Detalles del servicio...",
                    minLines = 2,
                    enabled = !state.isSaving,
                    onValueChange = onDescripcionChange
                )
            }

            FormCard(title = "Precio y rentabilidad") {
                ProductField(
                    label = "Precio de venta *",
                    value = state.precio,
                    placeholder = "Ej: 21.000",
                    isNumber = true,
                    prefix = "$",
                    enabled = !state.isSaving,
                    onValueChange = onPrecioChange
                )
                ProductField(
                    label = "Costo estimado",
                    value = state.costo,
                    placeholder = "Ej: 12.000",
                    isNumber = true,
                    prefix = "$",
                    enabled = !state.isSaving,
                    onValueChange = onCostoChange
                )

                Text(
                    text = "Cálculo automático",
                    color = TextGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ResultTile(
                        label = "Ganancia estimada",
                        value = gananciaStr,
                        color = if (state.isProfitable) GreenText else RedText,
                        background = if (state.isProfitable) GreenBg else RedBg,
                        modifier = Modifier.weight(1f)
                    )
                    ResultTile(
                        label = "Margen estimado",
                        value = margenStr,
                        color = if (state.isProfitable) PurpleText else RedText,
                        background = if (state.isProfitable) PurpleBg else RedBg,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (state.isProduct) {
                FormCard(title = "Inventario") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ProductField(
                            label = "Stock actual",
                            value = state.stockActual,
                            isNumber = true,
                            enabled = !state.isSaving,
                            onValueChange = onStockActualChange,
                            modifier = Modifier.weight(1f)
                        )
                        ProductField(
                            label = "Stock mínimo",
                            value = state.stockMinimo,
                            isNumber = true,
                            enabled = !state.isSaving,
                            onValueChange = onStockMinimoChange,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Estado activo", fontWeight = FontWeight.SemiBold, color = DarkBlue, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Visible y disponible para registrar ventas", color = TextMuted, fontSize = 13.sp)
                    }
                    Switch(
                        checked = state.isActive,
                        onCheckedChange = onIsActiveChange,
                        enabled = !state.isSaving,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = PurpleAi,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = BorderGray,
                            uncheckedBorderColor = Color.Transparent
                        )
                    )
                }
            }

            state.errorMessage?.let { message ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = RedBg)
                ) {
                    Text(
                        text = message,
                        color = RedText,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            if (state.isSaving) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)),
                    color = PurpleAi,
                    trackColor = PurpleBg
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateBack,
                    enabled = !state.isSaving,
                    modifier = Modifier.weight(1f).height(54.dp),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, BorderGray),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextGray)
                ) {
                    Text("Cancelar", fontWeight = FontWeight.Bold)
                }
                GradientButton(
                    text = if (state.isSaving) "Guardando..." else "Guardar",
                    onClick = onSave,
                    enabled = !state.isSaving,
                    modifier = Modifier.weight(1f).height(54.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
@Composable
private fun ProductPhotoCard(
    isProduct: Boolean,
    enabled: Boolean,
    selectedPhoto: String?,
    storedPhotoPath: String?,
    onPhotoChange: (String?) -> Unit
) {
    val itemLabel = if (isProduct) "producto" else "servicio"
    val context = LocalContext.current
    var pendingCapture by rememberSaveable { mutableStateOf<String?>(null) }
    var photoError by rememberSaveable { mutableStateOf<String?>(null) }

    fun replacePhoto(newPhoto: Uri?) {
        val newValue = newPhoto?.toString()
        onPhotoChange(newValue)
        photoError = null
        if (selectedPhoto != newValue) {
            deleteTemporaryProductPhoto(context, selectedPhoto)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) replacePhoto(uri)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { saved ->
        val captured = pendingCapture
        pendingCapture = null
        if (saved && captured != null) {
            replacePhoto(Uri.parse(captured))
        } else {
            deleteTemporaryProductPhoto(context, captured)
        }
    }

    val photo = selectedPhoto
    val canSelect = enabled && pendingCapture == null

    FormCard(title = "Foto del $itemLabel (opcional)") {
        if (photo == null && storedPhotoPath != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(LightBg),
                contentAlignment = Alignment.Center
            ) {
                StoredProductPhoto(
                    path = storedPhotoPath,
                    description = "Foto guardada del $itemLabel",
                    modifier = Modifier.fillMaxSize(),
                    fallback = {
                        Text(
                            text = "Foto guardada del $itemLabel",
                            color = TextGray,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                )
            }
        } else if (photo == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(PurpleBg, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("Agrega una foto para identificar tu $itemLabel", color = PurpleText)
            }
        } else {
            key(photo) {
                ProductPhotoPreview(
                    uri = Uri.parse(photo),
                    description = "Vista previa de la foto del $itemLabel"
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = {
                    photoError = null
                    runCatching {
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }.onFailure {
                        photoError = "No se pudo abrir la galería."
                    }
                },
                enabled = canSelect,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PurpleText)
            ) {
                Text("Galería")
            }

            OutlinedButton(
                onClick = {
                    photoError = null
                    runCatching {
                        val capture = createProductPhotoUri(context)
                        pendingCapture = capture.toString()
                        cameraLauncher.launch(capture)
                    }.onFailure {
                        deleteTemporaryProductPhoto(context, pendingCapture)
                        pendingCapture = null
                        photoError = "No se pudo abrir la cámara. Puedes usar la galería."
                    }
                },
                enabled = canSelect,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PurpleText)
            ) {
                Text("Cámara")
            }
        }

        if (photo != null || storedPhotoPath != null) {
            TextButton(
                onClick = { replacePhoto(null) },
                enabled = canSelect
            ) {
                Text("Quitar foto", color = RedText)
            }
        }

        photoError?.let { Text(it, color = RedText, fontSize = 13.sp) }
    }
}

@Composable
private fun ProductPhotoPreview(uri: Uri, description: String) {
    val resolver = LocalContext.current.contentResolver
    val preview by produceState<Result<ImageBitmap>?>(null, resolver, uri) {
        value = withContext(Dispatchers.IO) {
            runCatching { decodeProductPreview(resolver, uri).asImageBitmap() }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(LightBg),
        contentAlignment = Alignment.Center
    ) {
        val loaded = preview
        val bitmap = loaded?.getOrNull()
        when {
            loaded == null -> CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = PurpleAi,
                strokeWidth = 2.dp
            )
            bitmap != null -> Image(
                bitmap = bitmap,
                contentDescription = description,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
            else -> Text(
                text = "No se pudo mostrar la foto. Intenta elegir otra.",
                color = RedText,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

private fun decodeProductPreview(resolver: android.content.ContentResolver, uri: Uri): Bitmap {
    val maxSide = 960
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        return ImageDecoder.decodeBitmap(ImageDecoder.createSource(resolver, uri)) { decoder, info, _ ->
            val size = info.size
            val factor = minOf(1.0, maxSide.toDouble() / maxOf(size.width, size.height))
            decoder.setTargetSize(
                (size.width * factor).toInt().coerceAtLeast(1),
                (size.height * factor).toInt().coerceAtLeast(1)
            )
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        }
    }

    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
    require(bounds.outWidth > 0 && bounds.outHeight > 0)

    var sample = 1
    while (maxOf(bounds.outWidth, bounds.outHeight) / sample > maxSide) sample *= 2
    val options = BitmapFactory.Options().apply { inSampleSize = sample }
    val bitmap = requireNotNull(
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
    )
    val orientation = runCatching {
        resolver.openInputStream(uri)?.use {
            ExifInterface(it).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
        }
    }.getOrNull()
    val rotation = when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90f
        ExifInterface.ORIENTATION_ROTATE_180 -> 180f
        ExifInterface.ORIENTATION_ROTATE_270 -> 270f
        else -> return bitmap
    }

    val matrix = Matrix().apply { postRotate(rotation) }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

class ProductPhotoFileProvider : FileProvider()

private fun createProductPhotoUri(context: Context): Uri {
    val directory = File(context.cacheDir, "product_photos")
    check(directory.isDirectory || directory.mkdirs())
    val file = File.createTempFile("product_", ".jpg", directory)

    return try {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.productphotos",
            file
        )
    } catch (error: Exception) {
        file.delete()
        throw error
    }
}

private fun deleteTemporaryProductPhoto(context: Context, value: String?) {
    if (value == null) return
    val uri = Uri.parse(value)
    if (uri.scheme == "content" && uri.authority == "${context.packageName}.productphotos") {
        runCatching { context.contentResolver.delete(uri, null, null) }
    }
}

@Composable
private fun FormCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(title, color = DarkBlue, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            content()
        }
    }
}

@Composable
private fun TypePill(
    text: String,
    icon: ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val contentColor = if (isSelected) Color.White else TextGray
    Row(
        modifier = modifier
            .height(46.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) Brush.horizontalGradient(listOf(GradientStart, GradientEnd))
                else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
            )
            .clickable { onClick() },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            color = contentColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun ResultTile(
    label: String,
    value: String,
    color: Color,
    background: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(background, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Text(label, color = TextGray, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, color = color, fontWeight = FontWeight.Bold, fontSize = 18.sp, maxLines = 1)
    }
}

@Composable
private fun ProductField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    isNumber: Boolean = false,
    prefix: String? = null,
    minLines: Int = 1,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        label = { Text(label, color = TextMuted) },
        placeholder = placeholder?.let { { Text(it, color = Color(0xFFCBD5E1)) } },
        prefix = prefix?.let { { Text(it, color = TextGray) } },
        modifier = modifier.fillMaxWidth(),
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
        ),
        singleLine = minLines == 1,
        minLines = minLines,
        keyboardOptions = if (isNumber) KeyboardOptions(keyboardType = KeyboardType.Number) else KeyboardOptions.Default
    )
}

/** Botón principal con degradado morado, igual al acceso a la IA del Dashboard. */
@Composable
private fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .alpha(if (enabled) 1f else 0.55f)
                .background(Brush.horizontalGradient(listOf(GradientStart, GradientEnd)))
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}
