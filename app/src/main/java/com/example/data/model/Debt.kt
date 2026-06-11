package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "debts")
data class Debt(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val totalAmount: Double,
    val remainingAmount: Double,
    val interestRate: Double, // annual interest percentage, e.g., 12.0
    val minimumMonthlyPayment: Double,
    val notes: String = ""
)
