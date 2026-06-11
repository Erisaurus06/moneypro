package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.FinanceDatabase
import com.example.data.repository.FinanceRepository
import com.example.ui.screens.CalculatorsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DebtsAndSavingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.FinanceViewModel
import com.example.viewmodel.FinanceViewModelFactory

class MainActivity : ComponentActivity() {

    // Initialize Database and Repositories lazily
    private val database by lazy { FinanceDatabase.getDatabase(applicationContext) }
    private val repository by lazy { FinanceRepository(database.financeDao()) }
    
    // Instantiate viewmodel safely using factory
    private val viewModel: FinanceViewModel by viewModels {
        FinanceViewModelFactory(repository)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Dynamic immersive edge-to-edge display matching user system configs
        enableEdgeToEdge()
        
        setContent {
            MyApplicationTheme {
                var selectedTab by remember { mutableStateOf(0) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Text(
                                    text = when (selectedTab) {
                                        0 -> "RESUMEN FINANCIERO"
                                        1 -> "SIMULADORES FISCALES"
                                        else -> "MIS METAS Y DEUDAS"
                                    },
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 2.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background
                            ),
                            modifier = Modifier.testTag("app_top_bar")
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 8.dp,
                            modifier = Modifier
                                .navigationBarsPadding()
                                .testTag("bottom_nav_bar")
                        ) {
                            // Tab 1: Ledger Dashboard
                            NavigationBarItem(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                icon = {
                                    Icon(
                                        imageVector = if (selectedTab == 0) Icons.Filled.AccountBalanceWallet else Icons.Outlined.AccountBalanceWallet,
                                        contentDescription = "Resumen"
                                    )
                                },
                                label = { Text("Resumen", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                modifier = Modifier.testTag("tab_item_summary")
                            )

                            // Tab 2: Mortgage and Tax dynamic calculators
                            NavigationBarItem(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                icon = {
                                    Icon(
                                        imageVector = if (selectedTab == 1) Icons.Filled.Calculate else Icons.Outlined.Calculate,
                                        contentDescription = "Calculadoras"
                                    )
                                },
                                label = { Text("Simuladores", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                modifier = Modifier.testTag("tab_item_calculators")
                            )

                            // Tab 3: Savings Goals and Debts tracking lists
                            NavigationBarItem(
                                selected = selectedTab == 2,
                                onClick = { selectedTab = 2 },
                                icon = {
                                    Icon(
                                        imageVector = if (selectedTab == 2) Icons.Filled.Savings else Icons.Outlined.Savings,
                                        contentDescription = "Planificador"
                                    )
                                },
                                label = { Text("Metas/Pasivos", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                modifier = Modifier.testTag("tab_item_planner")
                            )
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.background,
                    contentWindowInsets = WindowInsets.safeDrawing
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (selectedTab) {
                            0 -> DashboardScreen(viewModel = viewModel)
                            1 -> CalculatorsScreen(viewModel = viewModel)
                            2 -> DebtsAndSavingsScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
