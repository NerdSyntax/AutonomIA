package com.nerdsyntax.juntalucas.feature.onboarding.data

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.util.UUID

object OnboardingSampleData {

    suspend fun generarDatosDePrueba(uid: String, fechaDestino: String) {
        val db = FirebaseFirestore.getInstance()

        val mesBase = LocalDate.parse(fechaDestino)
        val today = mesBase.plusDays(14).toString()
        val yesterday = mesBase.plusDays(13).toString()

        val timestamp = FieldValue.serverTimestamp()
        val millis = System.currentTimeMillis()

        val prod1Id = UUID.randomUUID().toString()
        val prod2Id = UUID.randomUUID().toString()
        val sale1Id = UUID.randomUUID().toString()
        val sale2Id = UUID.randomUUID().toString()
        val expense1Id = UUID.randomUUID().toString()
        val batchProductos = db.batch()

        val prod1 = mapOf(
            "id" to prod1Id,
            "userId" to uid,
            "name" to "Audífonos Inalámbricos (Demo)",
            "isProduct" to true,
            "price" to 25000L,
            "unitCost" to 12000L,
            "active" to true,
            "trackStock" to true,
            "stock" to 10L,
            "category" to "Electrónica",
            "description" to "Producto generado automáticamente para pruebas.",
            "stockMinimum" to 2L,
            "unit" to "Unidad",
            "isAsset" to false,
            "isSample" to true,
            "createdAt" to timestamp,
            "createdAtMillis" to millis
        )

        val prod2 = mapOf(
            "id" to prod2Id,
            "userId" to uid,
            "name" to "Cargador Carga Rápida (Demo)",
            "isProduct" to true,
            "price" to 15000L,
            "unitCost" to 4000L,
            "active" to true,
            "trackStock" to true,
            "stock" to 5L,
            "category" to "Accesorios",
            "description" to "Servicio/Producto de demostración.",
            "stockMinimum" to 1L,
            "unit" to "Unidad",
            "isAsset" to false,
            "isSample" to true,
            "createdAt" to timestamp,
            "createdAtMillis" to millis
        )

        batchProductos.set(db.collection("users").document(uid).collection("products").document(prod1Id), prod1)
        batchProductos.set(db.collection("users").document(uid).collection("products").document(prod2Id), prod2)
        batchProductos.commit().await()

        val batchMovimientos = db.batch()

        val sale1 = mapOf(
            "id" to sale1Id,
            "userId" to uid,
            "productId" to prod1Id,
            "productName" to "Audífonos Inalámbricos (Demo)",
            "quantity" to 1L,
            "unitPrice" to 25000L,
            "unitCost" to 12000L,
            "discount" to 0L,
            "total" to 25000L,
            "saleDate" to today,
            "paymentMethod" to "TRANSFER",
            "note" to "Venta de prueba",
            "stockTracked" to false,
            "createdAt" to timestamp,
            "createdAtMillis" to millis,
            "isSample" to true,
            "draft" to mapOf(
                "id" to sale1Id,
                "productId" to prod1Id,
                "productName" to "Audífonos Inalámbricos (Demo)",
                "quantity" to 1L,
                "unitPrice" to 25000L,
                "unitCost" to 12000L,
                "discount" to 0L,
                "total" to 25000L,
                "saleDate" to today,
                "paymentMethod" to "TRANSFER",
                "note" to "Venta de prueba"
            )
        )

        val sale2 = mapOf(
            "id" to sale2Id,
            "userId" to uid,
            "productId" to prod2Id,
            "productName" to "Cargador Carga Rápida (Demo)",
            "quantity" to 2L,
            "unitPrice" to 7500L,
            "unitCost" to 4000L,
            "discount" to 0L,
            "total" to 15000L,
            "saleDate" to yesterday,
            "paymentMethod" to "CASH",
            "note" to "Venta de prueba",
            "stockTracked" to false,
            "createdAt" to timestamp,
            "createdAtMillis" to millis,
            "isSample" to true,
            "draft" to mapOf(
                "id" to sale2Id,
                "productId" to prod2Id,
                "productName" to "Cargador Carga Rápida (Demo)",
                "quantity" to 2L,
                "unitPrice" to 7500L,
                "unitCost" to 4000L,
                "discount" to 0L,
                "total" to 15000L,
                "saleDate" to yesterday,
                "paymentMethod" to "CASH",
                "note" to "Venta de prueba"
            )
        )

        val exp1 = mapOf(
            "id" to expense1Id,
            "userId" to uid,
            "description" to "Pago proveedor (Ejemplo)",
            "category" to "Inventario",
            "amount" to 8000L,
            "expenseDate" to today,
            "type" to "VARIABLE",
            "paymentMethod" to "CASH",
            "note" to "Gasto de prueba",
            "createdAt" to timestamp,
            "createdAtMillis" to millis,
            "isSample" to true,
            "draft" to mapOf(
                "id" to expense1Id,
                "description" to "Pago proveedor (Ejemplo)",
                "category" to "Inventario",
                "amount" to 8000L,
                "expenseDate" to today,
                "type" to "VARIABLE",
                "paymentMethod" to "CASH",
                "note" to "Gasto de prueba"
            )
        )

        batchMovimientos.set(db.collection("users").document(uid).collection("sales").document(sale1Id), sale1)
        batchMovimientos.set(db.collection("users").document(uid).collection("sales").document(sale2Id), sale2)
        batchMovimientos.set(db.collection("users").document(uid).collection("expenses").document(expense1Id), exp1)

        batchMovimientos.commit().await()
    }
}