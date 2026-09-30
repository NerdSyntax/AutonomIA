package com.nerdsyntax.juntalucas.core.data

data class RemoteData<T>(val value: T, val fromCache: Boolean = false)

class DataValidationException(message: String) : IllegalArgumentException(message)
