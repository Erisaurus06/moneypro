package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Debt
import com.example.data.model.SavingsGoal
import com.example.ui.theme.DebtOrange
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.SavingsTeal
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.clickable
import com.example.viewmodel.FinanceViewModel

@Composable
fun DebtsAndSavingsScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val savingsGoals by viewModel.savingsGoals.collectAsState()
    val debts by viewModel.debts.collectAsState()

    var showAddGoalDialog by remember { mutableStateOf(false) }
    var showAddDebtDialog by remember { mutableStateOf(false) }

    var activeSubSection by remember { mutableStateOf(0) } // 0 = Ahorros, 1 = Deudas

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 88.dp)
    ) {
        // --- CHIP TOGGLER ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { activeSubSection = 0 },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeSubSection == 0) MaterialTheme.colorScheme.primary else Color.Transparent,
                        contentColor = if (activeSubSection == 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Rounded.Savings, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Metas Ahorro", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = { activeSubSection = 1 },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeSubSection == 1) MaterialTheme.colorScheme.primary else Color.Transparent,
                        contentColor = if (activeSubSection == 1) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Mis Deudas", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- SUB-SECTION HEADERS WITH REGISTER BUTTONS ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val headerTitle = if (activeSubSection == 0) "Planes de Ahorro Activos" else "Control de Deudas Totales"
                val totalNumberText = if (activeSubSection == 0) "${savingsGoals.size} metas" else "${debts.size} activas"

                Column {
                    Text(
                        text = headerTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = totalNumberText,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        fontSize = 13.sp
                    )
                }

                FilledTonalButton(
                    onClick = {
                        if (activeSubSection == 0) showAddGoalDialog = true else showAddDebtDialog = true
                    },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Nuevo", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // --- RENDER DYNAMIC LISTS ---
        if (activeSubSection == 0) {
            // SAVINGS SECTION list
            if (savingsGoals.isEmpty()) {
                item {
                    EmptyStateWrapper(
                        title = "No hay planes de ahorro",
                        description = "Crea tu primer plan (como tu fondo de emergencia) para empezar a acumular saldo.",
                        icon = Icons.Rounded.Savings
                    )
                }
            } else {
                items(savingsGoals, key = { it.id }) { goal ->
                    var showDepositDialog by remember { mutableStateOf(false) }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("savings_goal_${goal.id}"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1.0f)) {
                                    Text(
                                        text = goal.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (goal.targetDate.isNotBlank()) {
                                        Text(
                                            text = "Fecha estimada: ${goal.targetDate}",
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { viewModel.deleteSavingsGoal(goal) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Borrar meta", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                                    }
                                }
                            }

                            // Progress values
                            val progress = if (goal.targetAmount > 0) (goal.currentAmount / goal.targetAmount).toFloat() else 0f
                            val progressPercent = (progress * 100).toInt()

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column {
                                    Text("PROGRESO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                                    Text(
                                        text = "${String.format("$%,.0f", goal.currentAmount)} de ${String.format("$%,.0f", goal.targetAmount)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                                Text("$progressPercent%", fontWeight = FontWeight.Black, fontSize = 20.sp, color = SavingsTeal)
                            }

                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp)),
                                color = SavingsTeal,
                                trackColor = SavingsTeal.copy(alpha = 0.15f)
                            )

                            // Actions
                            Button(
                                onClick = { showDepositDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SavingsTeal, contentColor = Color.White)
                            ) {
                                Icon(Icons.Default.Savings, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Aportar / Ahorrar", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }

                    if (showDepositDialog) {
                        ActionAmountDialog(
                            title = "Añadir Ahorro a Meta",
                            description = "Elige la cantidad que deseas transferir de tu balance a tu meta de ahorro: '${goal.name}'",
                            onDismiss = { showDepositDialog = false },
                            onConfirm = { amt ->
                                viewModel.addSavingsDeposit(goal, amt)
                                showDepositDialog = false
                            }
                        )
                    }
                }
            }
        } else {
            // DEBTS SECTION list
            if (debts.isEmpty()) {
                item {
                    EmptyStateWrapper(
                        title = "No hay deudas registradas",
                        description = "¡Genial! No tienes deudas registradas. Si tienes créditos bancarios o personales, agrégalos para simular su pago amortizado.",
                        icon = Icons.Rounded.Shield
                    )
                }
            } else {
                items(debts, key = { it.id }) { debt ->
                    var showRepayDialog by remember { mutableStateOf(false) }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("debt_card_${debt.id}"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = debt.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(
                                            text = "Tasa interés: ${debt.interestRate}% anual",
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                            fontSize = 11.sp
                                        )
                                        Text(
                                            text = "Pago Mín: $${debt.minimumMonthlyPayment.toInt()}/mes",
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { viewModel.deleteDebt(debt) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Borrar deuda", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.4f))
                                }
                            }

                            // Math progress
                            val paidPercent = if (debt.totalAmount > 0) {
                                ((debt.totalAmount - debt.remainingAmount) / debt.totalAmount).toFloat().coerceIn(0f, 1f)
                            } else 1f
                            val progressPercent = (paidPercent * 100).toInt()

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column {
                                    Text("RESTANTE DE DEUDA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                                    Text(
                                        text = "${String.format("$%,.0f", debt.remainingAmount)} de ${String.format("$%,.0f", debt.totalAmount)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                                Text("Abonado $progressPercent%", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DebtOrange)
                            }

                            LinearProgressIndicator(
                                progress = { 1f - paidPercent }, // displays remaining debit ratio visually
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp)),
                                color = ExpenseRed,
                                trackColor = IncomeGreen.copy(alpha = 0.15f)
                            )

                            // Actions
                            Button(
                                onClick = { showRepayDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DebtOrange, contentColor = Color.White)
                            ) {
                                Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Registrar Abono ($)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }

                    if (showRepayDialog) {
                        ActionAmountDialog(
                            title = "Hacer Abono a Deuda",
                            description = "Ingresa la cantidad amortizada que has abonado a la deuda '${debt.name}'. Se descontará de tu pasivo acumulado.",
                            onDismiss = { showRepayDialog = false },
                            onConfirm = { amt ->
                                viewModel.makeDebtPayment(debt, amt)
                                showRepayDialog = false
                            }
                        )
                    }
                }
            }
        }
    }

    // --- ADD DIALOG FOR SAVINGS GOALS ---
    if (showAddGoalDialog) {
        AddSavingsGoalDialog(
            onDismiss = { showAddGoalDialog = false },
            onAdd = { name, target, initial, targetDate, notes ->
                viewModel.addSavingsGoal(name, target, initial, targetDate, notes)
                showAddGoalDialog = false
            }
        )
    }

    // --- ADD DIALOG FOR DEBT ---
    if (showAddDebtDialog) {
        AddDebtDialog(
            onDismiss = { showAddDebtDialog = false },
            onAdd = { name, total, remaining, rate, minPay, notes ->
                viewModel.addDebt(name, total, remaining, rate, minPay, notes)
                showAddDebtDialog = false
            }
        )
    }
}


@Composable
fun EmptyStateWrapper(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                modifier = Modifier.size(54.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
        }
    }
}


@Composable
fun ActionAmountDialog(
    title: String,
    description: String,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double) -> Unit
) {
    var amountStr by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Monto ($ MXN)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (showError) {
                    Text("Ingresa una cantidad numérica válida mayor que cero.", color = MaterialTheme.colorScheme.error, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amt = amountStr.toDoubleOrNull()
                            if (amt != null && amt > 0) {
                                onConfirm(amt)
                            } else {
                                showError = true
                            }
                        }
                    ) {
                        Text("Confirmar")
                    }
                }
            }
        }
    }
}


@Composable
fun AddSavingsGoalDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, target: Double, initial: Double, date: String, notes: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var targetStr by remember { mutableStateOf("") }
    var initialStr by remember { mutableStateOf("0") }
    var date by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    var showError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Crear Meta de Ahorro", fontWeight = FontWeight.Bold, fontSize = 18.sp)

                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre de la Meta") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = targetStr, onValueChange = { targetStr = it }, label = { Text("Monto Objetivo ($)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = initialStr, onValueChange = { initialStr = it }, label = { Text("Saldo Inicial ($)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Plazo Objetivo (Ej. Dic 2026)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notas") }, modifier = Modifier.fillMaxWidth())

                if (showError) {
                    Text("Favor de llenar campos obligatorios con valores correctos.", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val target = targetStr.toDoubleOrNull()
                            val initial = initialStr.toDoubleOrNull() ?: 0.0
                            if (name.isNotBlank() && target != null && target >= 0.0) {
                                onAdd(name, target, initial, date, notes)
                            } else {
                                showError = true
                            }
                        }
                    ) {
                        Text("Crear")
                    }
                }
            }
        }
    }
}


@Composable
fun AddDebtDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, total: Double, remaining: Double, rate: Double, minPay: Double, notes: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var totalStr by remember { mutableStateOf("") }
    var remainingStr by remember { mutableStateOf("") }
    var rateStr by remember { mutableStateOf("") }
    var minPayStr by remember { mutableStateOf("0") }
    var notes by remember { mutableStateOf("") }

    var showError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(11.dp)
            ) {
                Text("Añadir Nueva Deuda", fontWeight = FontWeight.Bold, fontSize = 18.sp)

                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Acreedor / Banco / Nombre") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = totalStr, onValueChange = { totalStr = it }, label = { Text("Dinero Total de Deuda ($)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = remainingStr, onValueChange = { remainingStr = it }, label = { Text("Saldo Restante Actual ($)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = rateStr, onValueChange = { rateStr = it }, label = { Text("Tasa de Interés (% Anual)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = minPayStr, onValueChange = { minPayStr = it }, label = { Text("Pago Mínimo Mensual ($)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Comentarios / Fecha de Pago") }, modifier = Modifier.fillMaxWidth())

                if (showError) {
                    Text("Revisa los montos numéricos ingresados.", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val total = totalStr.toDoubleOrNull()
                            val remaining = remainingStr.toDoubleOrNull() ?: total ?: 0.0
                            val rate = rateStr.toDoubleOrNull() ?: 0.0
                            val minPay = minPayStr.toDoubleOrNull() ?: 0.0

                            if (name.isNotBlank() && total != null && total >= 0.0) {
                                onAdd(name, total, remaining, rate, minPay, notes)
                            } else {
                                showError = true
                            }
                        }
                    ) {
                        Text("Registrar")
                    }
                }
            }
        }
    }
}
