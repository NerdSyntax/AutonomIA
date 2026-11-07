package com.nerdsyntax.juntalucas.feature.business.data

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.UUID

class FirebaseProductPhotoRepository(context: Context) {

    private val resolver = context.applicationContext.contentResolver
    private val storage by lazy { FirebaseStorage.getInstance() }

    suspend fun upload(
        uid: String,
        productId: String,
        selectedPhotoUri: String
    ): String {
        require(uid.isNotBlank() && '/' !in uid)
        require(productId.isNotBlank() && '/' !in productId)

        val uri = Uri.parse(selectedPhotoUri)
        require(uri.scheme == ContentResolver.SCHEME_CONTENT)

        val bytes = withContext(Dispatchers.IO) {
            compressPhoto(decodePhoto(uri))
        }

        // Un archivo nuevo evita sobrescribir la foto anterior antes de guardar.
        val path = "users/$uid/productPhotos/$productId/${UUID.randomUUID()}.jpg"
        val metadata = StorageMetadata.Builder()
            .setContentType("image/jpeg")
            .build()

        storage.reference.child(path).putBytes(bytes, metadata).await()
        return path
    }

    private fun decodePhoto(uri: Uri): Bitmap {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return ImageDecoder.decodeBitmap(
                ImageDecoder.createSource(resolver, uri)
            ) { decoder, info, _ ->
                val size = info.size
                val scale = minOf(
                    1.0,
                    MAX_SIDE.toDouble() / maxOf(size.width, size.height)
                )

                decoder.setTargetSize(
                    (size.width * scale).toInt().coerceAtLeast(1),
                    (size.height * scale).toInt().coerceAtLeast(1)
                )
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        }

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, bounds)
        }

        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            throw IOException("No se pudo leer la imagen.")
        }

        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / sample > MAX_SIDE) {
            sample *= 2
        }

        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        val bitmap = resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        } ?: throw IOException("No se pudo abrir la imagen.")

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

    private fun compressPhoto(original: Bitmap): ByteArray {
        var bitmap = original

        repeat(4) {
            for (quality in intArrayOf(88, 78, 68, 58)) {
                val output = ByteArrayOutputStream()
                if (!bitmap.compress(Bitmap.CompressFormat.JPEG, quality, output)) {
                    throw IOException("No se pudo comprimir la imagen.")
                }
                if (output.size() <= MAX_BYTES) return output.toByteArray()
            }

            bitmap = Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * 0.75).toInt().coerceAtLeast(1),
                (bitmap.height * 0.75).toInt().coerceAtLeast(1),
                true
            )
        }

        throw IOException("La imagen supera el tamaño permitido.")
    }

    private companion object {
        const val MAX_SIDE = 960
        const val MAX_BYTES = 300 * 1024
    }
}