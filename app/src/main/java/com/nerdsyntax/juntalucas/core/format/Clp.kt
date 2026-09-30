package com.nerdsyntax.juntalucas.core.format

import java.text.NumberFormat
import java.util.Locale

const val MAX_MONEY = 9_000_000_000_000L
const val MAX_QUANTITY = 1_000_000L

/** Whole CLP only. Dots are accepted exclusively as thousands separators. */
fun parseClp(text: String): Long? {
    val value = text.trim()
    if (!Regex("(?:[0-9]+|[1-9][0-9]{0,2}(?:\\.[0-9]{3})+)").matches(value)) return null
    return value.replace(".", "").toLongOrNull()?.takeIf { it in 0..MAX_MONEY }
}

fun parseQuantity(text: String): Long? = text.trim().takeIf { it.matches(Regex("[0-9]+")) }
    ?.toLongOrNull()?.takeIf { it in 1..MAX_QUANTITY }

fun formatClp(value: Long): String = "$" + NumberFormat.getIntegerInstance(Locale.forLanguageTag("es-CL")).format(value)
