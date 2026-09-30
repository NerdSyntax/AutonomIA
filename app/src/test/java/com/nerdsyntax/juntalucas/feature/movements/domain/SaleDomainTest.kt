package com.nerdsyntax.juntalucas.feature.movements.domain

import com.nerdsyntax.juntalucas.core.data.DataValidationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class SaleDomainTest {
    @Test fun calculatesWholeClpTotalAndRoundedTicket() {
        assertEquals(28_000L, calculateSaleTotal(2, 15_000, 2_000))
        val sales = listOf(10_001L, 10_000L).mapIndexed { index, total ->
            Sale(SaleDraft("$index", "u", "2026-09-01", "p", 1, total, 0, PaymentMethod.CASH), "Producto", null, index.toLong(), false)
        }
        assertEquals(SalesSummary(20_001, 10_001), summarizeSales(sales))
    }

    @Test fun rejectsNonPositiveQuantityPriceAndDiscountAboveSubtotal() {
        assertThrows(DataValidationException::class.java) { calculateSaleTotal(0, 100, 0) }
        assertThrows(DataValidationException::class.java) { calculateSaleTotal(1, 0, 0) }
        assertThrows(DataValidationException::class.java) { calculateSaleTotal(1, 100, 101) }
    }

    @Test fun emptySalesHaveZeroSummaryAndDatesStayWithinCivilMonths() {
        assertEquals(SalesSummary(0, 0), summarizeSales(emptyList()))
        assertEquals("2026-10", SaleDates.shiftMonth("2026-09", 1))
        assertEquals("2026-08", SaleDates.shiftMonth("2026-09", -1))
        assertEquals("01-09-2026", SaleDates.display("2026-09-01"))
    }
}
