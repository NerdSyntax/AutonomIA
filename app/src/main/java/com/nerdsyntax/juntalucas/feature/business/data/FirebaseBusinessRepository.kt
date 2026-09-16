package com.nerdsyntax.juntalucas.feature.business.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import com.nerdsyntax.juntalucas.feature.business.domain.Business
import com.nerdsyntax.juntalucas.feature.business.domain.BusinessAuthenticationException
import com.nerdsyntax.juntalucas.feature.business.domain.BusinessRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

class FirebaseBusinessRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : BusinessRepository {
    private fun currentUid(): String = auth.currentUser?.uid
        ?: throw BusinessAuthenticationException()

    private fun profile(uid: String) = firestore.collection("users").document(uid)
        .collection("business").document("profile")

    override suspend fun saveBusiness(business: Business): Result<Unit> = capture {
        val uid = currentUid()
        profile(uid).set(business, SetOptions.merge()).await()
        if (currentUid() != uid) throw BusinessAuthenticationException()
    }

    override suspend fun getBusiness(): Result<Business?> = capture {
        val uid = currentUid()
        val snapshot = profile(uid).get(Source.SERVER).await()
        if (currentUid() != uid) throw BusinessAuthenticationException()
        if (snapshot.exists()) snapshot.toObject(Business::class.java) else null
    }

    private suspend fun <T> capture(block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (error: Exception) {
        Result.failure(error)
    }
}
