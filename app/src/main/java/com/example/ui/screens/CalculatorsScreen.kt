package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Percent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.FinanceViewModel

@Composable
fun CalculatorsScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    var activeCalculatorTab by remember { mutableStateOf(0) } // 0 = Hipotecario, 1 = Impuestos

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- SUB-TABS SELECTOR CAP ---
        TabRow(
            selectedTabIndex = activeCalculatorTab,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = activeCalculatorTab == 0,
                onClick = { activeCalculatorTab = 0 },
                text = { Text("Crédito Hipotecario", fontWeight = FontWeight.Bold, fontSize = 14.sp) },
                icon = { Icon(Icons.Rounded.AccountBalance, contentDescription = null) }
            )
            Tab(
                selected = activeCalculatorTab == 1,
                onClick = { activeCalculatorTab = 1 },
                text = { Text("Impuestos (ISR)", fontWeight = FontWeight.Bold, fontSize = 14.sp) },
                icon = { Icon(Icons.Rounded.Percent, contentDescription = null) }
            )
        }

        // --- RENDER SELECTED CALCULATOR ---
        when (activeCalculatorTab) {
            0 -> MortgageCalculator(viewModel = viewModel)
            1 -> TaxCalculator(viewModel = viewModel)
        }
    }
}

// --- MORTGAGE CALCULATOR ---
@Composable
fun MortgageCalculator(viewModel: FinanceViewModel) {
    val homeValue by viewModel.mortgageHomeValue.collectAsState()
    val downPayment by viewModel.mortgageDownPayment.collectAsState()
    val rate by viewModel.mortgageInterestRate.collectAsState()
    val termYears by viewModel.mortgageTermYears.collectAsState()

    var homeValueStr by remember { mutableStateOf(homeValue.toInt().toString()) }
    var downPaymentStr by remember { mutableStateOf(downPayment.toInt().toString()) }
    var rateStr by remember { mutableStateOf(rate.toString()) }
    var termYearsStr by remember { mutableStateOf(termYears.toString()) }

    val mortgageResult = viewModel.calculateMortgage()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Form Inputs
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Parámetros del Crédito",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    // Home Value
                    OutlinedTextField(
                        value = homeValueStr,
                        onValueChange = {
                            homeValueStr = it
                            val cleanVal = it.toDoubleOrNull() ?: 0.0
                            viewModel.updateMortgageInputs(cleanVal, downPayment, rate, termYears)
                        },
                        label = { Text("Valor de la Propiedad ($ MXN)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Down Payment (Enganche)
                    OutlinedTextField(
                        value = downPaymentStr,
                        onValueChange = {
                            downPaymentStr = it
                            val cleanVal = it.toDoubleOrNull() ?: 0.0
                            viewModel.updateMortgageInputs(homeValue, cleanVal, rate, termYears)
                        },
                        label = { Text("Enganche / Pago Inicial ($ MXN)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Interest Rate annual
                        OutlinedTextField(
                            value = rateStr,
                            onValueChange = {
                                rateStr = it
                                val cleanVal = it.toDoubleOrNull() ?: 0.0
                                viewModel.updateMortgageInputs(homeValue, downPayment, cleanVal, termYears)
                            },
                            label = { Text("Interés Anual (%)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )

                        // Term in Years
                        OutlinedTextField(
                            value = termYearsStr,
                            onValueChange = {
                                termYearsStr = it
                                val cleanVal = it.toIntOrNull() ?: 1
                                viewModel.updateMortgageInputs(homeValue, downPayment, rate, cleanVal)
                            },
                            label = { Text("Plazo (Años)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Summary Calculations Results Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Resultados del Análisis",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )

                    // Monthly Payment display
                    Column {
                        Text(
                            text = "PAGO MENSUAL ESTIMADO",
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = String.format("$%,.2f MXN", mortgageResult.monthlyPayment),
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f))

                    // Calculations Table
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Monto del Crédito", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f))
                            Text(String.format("$%,.0f", mortgageResult.loanAmount), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Total Intereses", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f))
                            Text(String.format("$%,.0f", mortgageResult.totalInterest), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total a Pagar", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f))
                            Text(String.format("$%,.0f", mortgageResult.totalPayment), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Relación Interés", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f))
                            Text(String.format("%.1f%%", mortgageResult.relationshipPrincipalInterest), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
            }
        }

        // Yearly Amortization Samples Table
        item {
            Text(
                text = "Tabla de Amortización Anual Simbólica",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 8.dp)
            )
        }

        // Header Table
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text("Año", modifier = Modifier.weight(0.4f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("Anualidad", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.End)
                Text("Interés", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.End)
                Text("Principal", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.End)
                Text("Saldo", modifier = Modifier.weight(1.2f), fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.End)
            }
        }

        items(mortgageResult.yearlyAmortizationSample) { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp, horizontal = 12.dp)
            ) {
                Text("${row.year}", modifier = Modifier.weight(0.4f), fontSize = 12.sp)
                Text(String.format("$%,.0f", row.annualPayment), modifier = Modifier.weight(1f), fontSize = 12.sp, textAlign = TextAlign.End)
                Text(String.format("$%,.0f", row.annualInterest), modifier = Modifier.weight(1f), fontSize = 12.sp, textAlign = TextAlign.End, color = MaterialTheme.colorScheme.error)
                Text(String.format("$%,.0f", row.annualPrincipal), modifier = Modifier.weight(1f), fontSize = 12.sp, textAlign = TextAlign.End, color = MaterialTheme.colorScheme.primary)
                Text(String.format("$%,.0f", row.remainingBalance), modifier = Modifier.weight(1.2f), fontSize = 12.sp, textAlign = TextAlign.End, fontWeight = FontWeight.Bold)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
        }
    }
}


// --- TAX CALCULATOR ---
@Composable
fun TaxCalculator(viewModel: FinanceViewModel) {
    val income by viewModel.taxIncomeInput.collectAsState()
    val isMonthly by viewModel.isTaxMonthly.collectAsState()
    val regime by viewModel.taxSelectedRegime.collectAsState()

    var incomeStr by remember { mutableStateOf(income.toInt().toString()) }

    val taxResult = viewModel.calculateTaxEstimation()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Form inputs
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Ingresos e Impuestos",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    // Regime selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                            .padding(2.dp)
                    ) {
                        Button(
                            onClick = { viewModel.updateTaxInputs(income, isMonthly, "ASALARIADO") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (regime == "ASALARIADO") MaterialTheme.colorScheme.primary else Color.Transparent,
                                contentColor = if (regime == "ASALARIADO") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Nómina/Asalariado", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { viewModel.updateTaxInputs(income, isMonthly, "RESICO") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (regime == "RESICO") MaterialTheme.colorScheme.primary else Color.Transparent,
                                contentColor = if (regime == "RESICO") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("RESICO/Autónomo", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Input Raw Revenue
                    OutlinedTextField(
                        value = incomeStr,
                        onValueChange = {
                            incomeStr = it
                            val cleanVal = it.toDoubleOrNull() ?: 0.0
                            viewModel.updateTaxInputs(cleanVal, isMonthly, regime)
                        },
                        label = { Text("Ingresos Brutos ($ MXN)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Time unit selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Periodicidad del ingreso:",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { viewModel.updateTaxInputs(income, true, regime) }
                        ) {
                            RadioButton(selected = isMonthly, onClick = { viewModel.updateTaxInputs(income, true, regime) })
                            Text("Mensual", fontSize = 13.sp)
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { viewModel.updateTaxInputs(income, false, regime) }
                        ) {
                            RadioButton(selected = !isMonthly, onClick = { viewModel.updateTaxInputs(income, false, regime) })
                            Text("Anual", fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Summary Analysis of Taxes Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Análisis del Ingreso Neto",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Ingreso Bruto", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Text(String.format("$%,.2f", taxResult.grossIncome), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Impuesto Retenido (ISR)", color = MaterialTheme.colorScheme.error)
                        Text(String.format("-$%,.2f", taxResult.isrTax), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                    }

                    if (regime == "RESICO") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("IVA Trasladado estim. (16%)", color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f))
                            Text(String.format("-$%,.2f", taxResult.vatTax), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f))
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("INGRESO NETO LIBRE", fontWeight = FontWeight.Black, fontSize = 14.sp)
                        Text(
                            String.format("$%,.2f MXN", taxResult.netIncome),
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tasa Impositiva Efectiva", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Text(String.format("%.2f%%", taxResult.effectiveTaxRatePercent), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        // Details Explain Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = taxResult.description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
