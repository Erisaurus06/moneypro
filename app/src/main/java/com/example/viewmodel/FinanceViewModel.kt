package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.Debt
import com.example.data.model.SavingsGoal
import com.example.data.model.Transaction
import com.example.data.repository.FinanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FinanceViewModel(private val repository: FinanceRepository) : ViewModel() {

    // --- ROOM OBSERVABLE STATE ---
    val transactions: StateFlow<List<Transaction>> = repository.allTransactions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val debts: StateFlow<List<Debt>> = repository.allDebts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val savingsGoals: StateFlow<List<SavingsGoal>> = repository.allSavingsGoals
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- AGGREGATIONS ---
    val balance: StateFlow<Double> = transactions
        .combine(MutableStateFlow(0.0)) { list, _ ->
            list.sumOf { if (it.isIncome) it.amount else -it.amount }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalIncome: StateFlow<Double> = transactions
        .combine(MutableStateFlow(0.0)) { list, _ ->
            list.filter { it.isIncome }.sumOf { it.amount }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalExpenses: StateFlow<Double> = transactions
        .combine(MutableStateFlow(0.0)) { list, _ ->
            list.filter { !it.isIncome }.sumOf { it.amount }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // --- PRE-SEED SEEDING TRIGGER ---
    init {
        viewModelScope.launch {
            // Seed default data if database is empty to show beautiful visual indicators initially
            transactions.collect { list ->
                if (list.isEmpty()) {
                    seedDefaultData()
                }
            }
        }
    }

    private suspend fun seedDefaultData() {
        // Seed standard transactions
        repository.insertTransaction(
            Transaction(
                title = "Salario Quincenal",
                amount = 18500.0,
                isIncome = true,
                category = "Sueldo",
                note = "Pago de nómina quincenal"
            )
        )
        repository.insertTransaction(
            Transaction(
                title = "Supermercado Semanal",
                amount = 2340.0,
                isIncome = false,
                category = "Alimentos",
                note = "Despensa para la semana"
            )
        )
        repository.insertTransaction(
            Transaction(
                title = "Renta Mensual",
                amount = 6500.0,
                isIncome = false,
                category = "Vivienda",
                note = "Pago del departamento"
            )
        )
        repository.insertTransaction(
            Transaction(
                title = "Reembolso de Almuerzo",
                amount = 450.0,
                isIncome = true,
                category = "Otros",
                note = "Reembolso de gastos de trabajo"
            )
        )

        // Seed default savings goals
        repository.insertSavingsGoal(
            SavingsGoal(
                name = "Fondo de Emergencias",
                targetAmount = 50000.0,
                currentAmount = 18500.0,
                targetDate = "Dic 2026",
                notes = "Fondo de seguridad para imprevistos"
            )
        )
        repository.insertSavingsGoal(
            SavingsGoal(
                name = "Viaje de Vacaciones",
                targetAmount = 25000.0,
                currentAmount = 8000.0,
                targetDate = "Ago 2026",
                notes = "Ahorros para el viaje de verano"
            )
        )

        // Seed default debts
        repository.insertDebt(
            Debt(
                name = "Tarjeta de Crédito Bancaria",
                totalAmount = 15000.0,
                remainingAmount = 6500.0,
                interestRate = 34.5,
                minimumMonthlyPayment = 1200.0,
                notes = "Pagar más del mínimo siempre"
            )
        )
        repository.insertDebt(
            Debt(
                name = "Crédito de Computadora",
                totalAmount = 12000.0,
                remainingAmount = 4500.0,
                interestRate = 18.0,
                minimumMonthlyPayment = 850.0,
                notes = "Pagos sin intereses parciales"
            )
        )
    }

    // --- TRANSACTION OPERATION EXPOSURES ---
    fun addTransaction(title: String, amount: Double, isIncome: Boolean, category: String, note: String) {
        viewModelScope.launch {
            repository.insertTransaction(
                Transaction(
                    title = title,
                    amount = amount,
                    isIncome = isIncome,
                    category = category,
                    note = note
                )
            )
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun deleteTransactionById(id: Int) {
        viewModelScope.launch {
            repository.deleteTransactionById(id)
        }
    }

    // --- DEBT OPERATION EXPOSURES ---
    fun addDebt(name: String, totalAmount: Double, remainingAmount: Double, interestRate: Double, minPayment: Double, notes: String) {
        viewModelScope.launch {
            repository.insertDebt(
                Debt(
                    name = name,
                    totalAmount = totalAmount,
                    remainingAmount = remainingAmount,
                    interestRate = interestRate,
                    minimumMonthlyPayment = minPayment,
                    notes = notes
                )
            )
        }
    }

    fun updateDebtAmount(id: Int, currentAmount: Double) {
        viewModelScope.launch {
            repository.updateDebtRemainingAmount(id, currentAmount)
        }
    }

    fun makeDebtPayment(debt: Debt, paymentAmount: Double) {
        viewModelScope.launch {
            val newAmount = (debt.remainingAmount - paymentAmount).coerceAtLeast(0.0)
            repository.updateDebtRemainingAmount(debt.id, newAmount)
            
            // Add automatic ledger transaction record of the debt payment
            repository.insertTransaction(
                Transaction(
                    title = "Abono: ${debt.name}",
                    amount = paymentAmount,
                    isIncome = false,
                    category = "Deudas",
                    note = "Pago amortizado a deuda activa"
                )
            )
        }
    }

    fun deleteDebt(debt: Debt) {
        viewModelScope.launch {
            repository.deleteDebt(debt)
        }
    }

    // --- SAVINGS GOALS OPERATION EXPOSURES ---
    fun addSavingsGoal(name: String, targetAmount: Double, currentAmount: Double, targetDate: String, notes: String) {
        viewModelScope.launch {
            repository.insertSavingsGoal(
                SavingsGoal(
                    name = name,
                    targetAmount = targetAmount,
                    currentAmount = currentAmount,
                    targetDate = targetDate,
                    notes = notes
                )
            )
        }
    }

    fun addSavingsDeposit(goal: SavingsGoal, depositAmount: Double) {
        viewModelScope.launch {
            val newAmount = (goal.currentAmount + depositAmount).coerceAtMost(goal.targetAmount)
            repository.updateSavingsGoalCurrentAmount(goal.id, newAmount)
            
            // Log as expense from wallet to savings
            repository.insertTransaction(
                Transaction(
                    title = "Depósito: ${goal.name}",
                    amount = depositAmount,
                    isIncome = false,
                    category = "Ahorros",
                    note = "Aporte a meta de ahorro"
                )
            )
        }
    }

    fun deleteSavingsGoal(goal: SavingsGoal) {
        viewModelScope.launch {
            repository.deleteSavingsGoal(goal)
        }
    }


    // --- FINANCIAL SPECIALIST MATH CALCULATORS ---

    // 1. Mortgage (Crédito Hipotecario) Calculator State & Math
    private val _mortgageHomeValue = MutableStateFlow(2500000.0) // Casa de $2,500,000 por defecto
    val mortgageHomeValue = _mortgageHomeValue.asStateFlow()

    private val _mortgageDownPayment = MutableStateFlow(500000.0) // Enganche de $500,000 por defecto
    val mortgageDownPayment = _mortgageDownPayment.asStateFlow()

    private val _mortgageInterestRate = MutableStateFlow(10.5) // tasa anual por defecto 10.5%
    val mortgageInterestRate = _mortgageInterestRate.asStateFlow()

    private val _mortgageTermYears = MutableStateFlow(20) // Plazo por defecto 20 años
    val mortgageTermYears = _mortgageTermYears.asStateFlow()

    fun updateMortgageInputs(homeValue: Double, downPayment: Double, interestRate: Double, termYears: Int) {
        _mortgageHomeValue.value = homeValue
        _mortgageDownPayment.value = downPayment
        _mortgageInterestRate.value = interestRate
        _mortgageTermYears.value = termYears
    }

    // Calculations of mortgages:
    data class MortgageResult(
        val loanAmount: Double,
        val monthlyPayment: Double,
        val totalPayment: Double,
        val totalInterest: Double,
        val relationshipPrincipalInterest: Double, // % of interest out of total payments
        val yearlyAmortizationSample: List<YearlyAmortizationRow>
    )

    data class YearlyAmortizationRow(
        val year: Int,
        val annualPayment: Double,
        val annualInterest: Double,
        val annualPrincipal: Double,
        val remainingBalance: Double
    )

    fun calculateMortgage(): MortgageResult {
        val L = (_mortgageHomeValue.value - _mortgageDownPayment.value).coerceAtLeast(0.0)
        val annualRate = _mortgageInterestRate.value / 100.0
        val monthlyRate = annualRate / 12.0
        val years = _mortgageTermYears.value
        val N = (years * 12).coerceAtLeast(1)

        val monthlyPayment = if (L <= 0.0) {
            0.0
        } else if (monthlyRate <= 0.0) {
            L / N
        } else {
            L * (monthlyRate * Math.pow(1.0 + monthlyRate, N.toDouble())) / (Math.pow(1.0 + monthlyRate, N.toDouble()) - 1.0)
        }

        val totalPayment = monthlyPayment * N
        val totalInterest = (totalPayment - L).coerceAtLeast(0.0)
        val ratioInterest = if (totalPayment > 0) (totalInterest / totalPayment) * 100.0 else 0.0

        // Amortization Schedule generator (grouped by active Year)
        val schema = mutableListOf<YearlyAmortizationRow>()
        var balance = L
        var cumulativePaidInterest = 0.0
        var cumulativePaidPrincipal = 0.0

        for (year in 1..years) {
            var annualInterestPaidThisYear = 0.0
            var annualPrincipalPaidThisYear = 0.0
            var annualPaidThisYear = 0.0

            for (month in 1..12) {
                if (balance <= 0.0) break
                val interestPayment = balance * monthlyRate
                val principalPayment = (monthlyPayment - interestPayment).coerceAtMost(balance)
                balance = (balance - principalPayment).coerceAtLeast(0.0)

                annualInterestPaidThisYear += interestPayment
                annualPrincipalPaidThisYear += principalPayment
                annualPaidThisYear += (interestPayment + principalPayment)
            }

            schema.add(
                YearlyAmortizationRow(
                    year = year,
                    annualPayment = annualPaidThisYear,
                    annualInterest = annualInterestPaidThisYear,
                    annualPrincipal = annualPrincipalPaidThisYear,
                    remainingBalance = balance
                )
            )

            if (balance <= 0.0) break
        }

        return MortgageResult(
            loanAmount = L,
            monthlyPayment = monthlyPayment,
            totalPayment = totalPayment,
            totalInterest = totalInterest,
            relationshipPrincipalInterest = ratioInterest,
            yearlyAmortizationSample = schema
        )
    }


    // 2. Tax Approximator (Impuestos) State & Math
    private val _taxIncomeInput = MutableStateFlow(32000.0) // Ingreso mensual predeterminado: $32,000
    val taxIncomeInput = _taxIncomeInput.asStateFlow()

    private val _isTaxMonthly = MutableStateFlow(true)
    val isTaxMonthly = _isTaxMonthly.asStateFlow()

    private val _taxSelectedRegime = MutableStateFlow("RESICO") // "Sueldo y Salario" or "RESICO" (Simplified tax in Latam)
    val taxSelectedRegime = _taxSelectedRegime.asStateFlow()

    fun updateTaxInputs(income: Double, isMonthly: Boolean, regime: String) {
        _taxIncomeInput.value = income
        _isTaxMonthly.value = isMonthly
        _taxSelectedRegime.value = regime
    }

    data class TaxEstimationResult(
        val grossIncome: Double,
        val isrTax: Double,
        val vatTax: Double, // IVA included/applied
        val netIncome: Double,
        val effectiveTaxRatePercent: Double,
        val breakdownIsrPercent: Double,
        val description: String
    )

    fun calculateTaxEstimation(): TaxEstimationResult {
        val gross = _taxIncomeInput.value
        val regime = _taxSelectedRegime.value
        val isMonthly = _isTaxMonthly.value

        // Convert monthly income equivalent if needed to calculate base rates
        val monthlyEquivalent = if (isMonthly) gross else gross / 12.0

        var isrTax = 0.0
        var vatTax = 0.0
        var description = ""

        when (regime) {
            "RESICO" -> {
                // Simplified tax regime in Mexico (RESICO matches 1% to 2.5% max)
                val rate = when {
                    monthlyEquivalent <= 25000.0 -> 0.010 // 1%
                    monthlyEquivalent <= 50000.0 -> 0.011 // 1.1%
                    monthlyEquivalent <= 83333.0 -> 0.015 // 1.5%
                    monthlyEquivalent <= 208333.0 -> 0.020 // 2.0%
                    else -> 0.025 // 2.5%
                }
                isrTax = gross * rate
                vatTax = gross * 0.16 // Resico business pays VAT 16% typically
                description = "Bajo Régimen Simplificado (RESICO), pagas una tasa de ISR preferencial del ${(rate * 100).toFloat()}% sobre tu ingreso bruto, más el IVA traslativo del 16%."
            }
            "ASALARIADO" -> {
                // Sueldos y Salarios Progressive bracket model (LatAm standard ISR estimation monthly)
                // Simulated ISR brackets for Mexico/Colombia/Peru progressive scale:
                val mIsr = when {
                    monthlyEquivalent <= 8000.0 -> monthlyEquivalent * 0.019
                    monthlyEquivalent <= 15000.0 -> (8000.0 * 0.019) + (monthlyEquivalent - 8000.0) * 0.064
                    monthlyEquivalent <= 28000.0 -> (8000.0 * 0.019) + (7000.0 * 0.064) + (monthlyEquivalent - 15000.0) * 0.109
                    monthlyEquivalent <= 45000.0 -> (8000.0 * 0.019) + (7000.0 * 0.064) + (13000.0 * 0.109) + (monthlyEquivalent - 28000.0) * 0.179
                    monthlyEquivalent <= 85000.0 -> (8000.0 * 0.019) + (7000.0 * 0.064) + (13000.0 * 0.109) + (17000.0 * 0.179) + (monthlyEquivalent - 45000.0) * 0.2352
                    else -> (8000.0 * 0.019) + (7000.0 * 0.064) + (13000.0 * 0.109) + (17000.0 * 0.179) + (40000.0 * 0.2352) + (monthlyEquivalent - 85000.0) * 0.30
                }

                isrTax = if (isMonthly) mIsr else mIsr * 12.0
                vatTax = 0.0 // Asalariados do not charge/add VAT directly to salary
                description = "Régimen de Sueldos y Salarios. Aplica retención escalonada progresiva según las tarifas fiscales de ISR, sin cobro adicional de IVA."
            }
            else -> {
                // Honorarios / Actividad Empresarial Standard Brackets
                val rate = 0.25 // 25% average tax
                isrTax = gross * rate
                vatTax = gross * 0.16
                description = "Régimen de Actividad Profesional / Honorarios. Estimación fija basada en tasa ISR promedio del 25% y un IVA trasladado de 16%."
            }
        }

        val totalTaxes = isrTax + vatTax
        val net = (gross - totalTaxes).coerceAtLeast(0.0)
        val effRate = if (gross > 0) (totalTaxes / gross) * 100.0 else 0.0
        val isrRatio = if (gross > 0) (isrTax / gross) * 100.0 else 0.0

        return TaxEstimationResult(
            grossIncome = gross,
            isrTax = isrTax,
            vatTax = vatTax.toDouble(),
            netIncome = net,
            effectiveTaxRatePercent = effRate,
            breakdownIsrPercent = isrRatio,
            description = description
        )
    }
}

// Factory to inject repository safely
class FinanceViewModelFactory(private val repository: FinanceRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FinanceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FinanceViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
