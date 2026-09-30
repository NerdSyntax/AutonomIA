package com.nerdsyntax.juntalucas.feature.movements.ui.demo

import com.nerdsyntax.juntalucas.feature.movements.ui.MovementItem
import com.nerdsyntax.juntalucas.feature.movements.ui.MovementTab

/** Display fixtures, independent of the pending onboarding sample import. */
internal object MovementsDemoData {
    fun movements(tab: MovementTab): List<MovementItem> = if (tab == MovementTab.VENTAS) {
        listOf(
            MovementItem("1", "Torta de cumpleaños", "04-09-2026", "Transferencia", "+$65.000", true),
            MovementItem("2", "Cupcakes (6 docenas)", "04-09-2026", "Débito", "+$126.000", true),
            MovementItem("3", "Pie de limón (x4)", "03-09-2026", "Efectivo", "+$28.000", true),
            MovementItem("4", "Torta 3 pisos", "03-09-2026", "Transferencia", "+$120.000", true),
            MovementItem("5", "Alfajores (caja 12)", "02-09-2026", "Débito", "+$22.000", true)
        )
    } else {
        listOf(
            MovementItem("6", "Compra de harina y azúcar", "04-09-2026", "Efectivo", "-$32.400", false),
            MovementItem("7", "Gas butano", "03-09-2026", "Efectivo", "-$18.500", false),
            MovementItem("8", "Pago de arriendo local", "01-09-2026", "Transferencia", "-$282.600", false)
        )
    }
}
