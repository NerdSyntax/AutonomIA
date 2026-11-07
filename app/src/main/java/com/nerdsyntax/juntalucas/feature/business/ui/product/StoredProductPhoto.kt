package com.nerdsyntax.juntalucas.feature.business.ui.product

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

@Composable
internal fun StoredProductPhoto(
    path: String?,
    description: String,
    modifier: Modifier = Modifier,
    fallback: @Composable () -> Unit = {}
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid

    if (uid == null || path.isNullOrBlank() ||
        !path.startsWith("users/$uid/productPhotos/")
    ) {
        fallback()
        return
    }

    key(uid, path) {
        val bitmap by produceState<ImageBitmap?>(initialValue = null) {
            value = try {
                val bytes = FirebaseStorage.getInstance()
                    .reference
                    .child(path)
                    .getBytes(300L * 1024L)
                    .await()

                withContext(Dispatchers.Default) {
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        ?.asImageBitmap()
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                null
            }
        }

        val image = bitmap
        if (image != null) {
            Image(
                bitmap = image,
                contentDescription = description,
                modifier = modifier,
                contentScale = ContentScale.Fit
            )
        } else {
            fallback()
        }
    }
}