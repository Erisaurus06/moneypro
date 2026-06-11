package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val amount: Double,
    val isIncome: Boolean,
    val category: String, // e.g., "Alimentos", "Impuestos", "Servicios", "Deudas", "Ahorros", "Sueldo", "Otros"
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = ""
)
