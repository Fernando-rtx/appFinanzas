package com.example.util

import java.text.NumberFormat
import java.util.Locale

/**
 * Formateador de moneda centralizado (es_MX).
 * Se crea una sola vez para evitar allocaciones repetidas en cada recomposición.
 */
private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "MX"))

/**
 * Convierte una cantidad en centavos (Long) a su representación monetaria localizada.
 * Ejemplo: 1999 → "$19.99"
 */
fun Long.centsToCurrency(): String = currencyFormat.format(this / 100.0)

/**
 * Convierte un string numérico a centavos (Long), o null si no es un número válido.
 * Ejemplo: "19.99" → 1999L
 * Usa [Math.round] para evitar errores de punto flotante (19.99 * 100 → 1999, no 1998.999).
 */
fun String.toCentsOrNull(): Long? {
    val d = toDoubleOrNull() ?: return null
    return Math.round(d * 100)
}
