package com.nerdsyntax.juntalucas.core.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

internal fun FirebaseAuth.requireOwner(uid: String) {
    if (uid.isBlank() || currentUser?.uid != uid) throw DataValidationException("Tu sesión cambió. Vuelve a iniciar sesión.")
}

internal suspend fun <T> firestoreResult(block: suspend () -> T): Result<T> = try {
    Result.success(block())
} catch (cancelled: CancellationException) {
    throw cancelled
} catch (error: Exception) {
    Result.failure(error)
}

internal fun <T> Query.observeOwned(auth: FirebaseAuth, uid: String, map: (DocumentSnapshot) -> T) =
    callbackFlow<Result<RemoteData<List<T>>>> {
        try { auth.requireOwner(uid) } catch (error: Exception) {
            trySend(Result.failure(error)); close(); return@callbackFlow
        }
        val listener = addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
            if (auth.currentUser?.uid != uid) {
                trySend(Result.failure(DataValidationException("Tu sesión cambió.")))
                close()
            } else if (error != null) {
                trySend(Result.failure(error))
            } else if (snapshot != null) {
                trySend(runCatching { RemoteData(snapshot.documents.map(map), snapshot.metadata.isFromCache) })
            }
        }
        awaitClose { listener.remove() }
    }

fun Throwable.salesUserMessage(): String = when (this) {
    is DataValidationException -> message ?: "Revisa los datos ingresados."
    is FirebaseFirestoreException -> when (code) {
        FirebaseFirestoreException.Code.PERMISSION_DENIED -> "No tienes permiso para acceder a estos datos. Revisa la sesión y las reglas de Firebase."
        FirebaseFirestoreException.Code.UNAVAILABLE, FirebaseFirestoreException.Code.DEADLINE_EXCEEDED ->
            "No pudimos confirmar la operación con Firebase. Revisa tu conexión y reintenta."
        else -> "No pudimos completar la operación con Firebase. Intenta nuevamente."
    }
    else -> "No pudimos cargar o guardar los datos. Intenta nuevamente."
}
