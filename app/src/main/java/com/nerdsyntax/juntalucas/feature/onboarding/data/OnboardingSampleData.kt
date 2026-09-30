package com.nerdsyntax.juntalucas.feature.onboarding.data

import java.util.UUID

/** In-memory onboarding examples, retained for the pending movements integration. */
object OnboardingSampleData {
    var movements: List<OnboardingSampleMovement> = emptyList()
        private set

    fun initialize(startingPoint: String) {
        when (startingPoint) {
            "manual", "importar" -> {
                movements = emptyList()
            }
            "ejemplo" -> {
                movements = listOf(
                    OnboardingSampleMovement(description = "Venta pastel de chocolate", amount = 15000, type = "ingreso"),
                    OnboardingSampleMovement(description = "Compra de harina y huevos", amount = 4500, type = "gasto"),
                    OnboardingSampleMovement(description = "Venta 12 cupcakes surtidos", amount = 18000, type = "ingreso"),
                    OnboardingSampleMovement(description = "Pago de electricidad", amount = 22000, type = "gasto"),
                    OnboardingSampleMovement(description = "Venta torta de novios", amount = 65000, type = "ingreso")
                )
            }
        }
    }
}

data class OnboardingSampleMovement(
    val id: String = UUID.randomUUID().toString(),
    val description: String,
    val amount: Int,
    val type: String // "ingreso" o "gasto"
)
