package com.nerdsyntax.juntalucas.feature.auth.ui.common

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

internal fun Throwable.toAuthUserMessage(): String = when (this) {
    is FirebaseAuthWeakPasswordException -> "La contraseña es demasiado débil."
    is FirebaseAuthUserCollisionException -> "Ya existe una cuenta registrada con este correo."
    is FirebaseAuthInvalidUserException -> "No existe una cuenta válida con este correo."
    is FirebaseAuthInvalidCredentialsException -> "Correo o contraseña incorrectos."
    is FirebaseTooManyRequestsException -> "Demasiados intentos. Intenta nuevamente más tarde."
    is FirebaseAuthRecentLoginRequiredException ->
        "Por seguridad debes volver a iniciar sesión antes de realizar esta acción."
    is FirebaseNetworkException -> "No se pudo conectar. Revisa tu conexión a Internet."
    else -> message ?: "Ocurrió un error inesperado."
}
