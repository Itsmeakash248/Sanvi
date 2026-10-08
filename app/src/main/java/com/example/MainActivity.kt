package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AddEditProductDialog
import com.example.ui.components.BillReceiptDialog
import com.example.ui.components.ShopTopBar
import com.example.ui.screens.BillHistoryScreen
import com.example.ui.screens.BillingScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ProductsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ShopViewModel

enum class ShopScreen(val label: String) {
    DASHBOARD("Home"),
    BILLING("Billing"),
    PRODUCTS("Stock"),
    HISTORY("History"),
    SETTINGS("Shop")
}

class MainActivity : ComponentActivity() {
    private val viewModel: ShopViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainApp(viewModel: ShopViewModel) {
    var currentScreen by remember { mutableStateOf(ShopScreen.DASHBOARD) }
    var isAddingProductFromDashboard by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val userRole by viewModel.userRole.collectAsStateWithLifecycle()
    val shopProfile by viewModel.shopProfile.collectAsStateWithLifecycle()
    val completedBill by viewModel.completedBill.collectAsStateWithLifecycle()

    // BackHandler: If on a subscreen, navigate back to Dashboard
    if (currentScreen != ShopScreen.DASHBOARD) {
        BackHandler {
            currentScreen = ShopScreen.DASHBOARD
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            ShopTopBar(
                shopProfile = shopProfile,
                userRole = userRole,
                onRoleChange = { viewModel.switchRole(it) }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                NavigationBarItem(
                    selected = currentScreen == ShopScreen.DASHBOARD,
                    onClick = { currentScreen = ShopScreen.DASHBOARD },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text(ShopScreen.DASHBOARD.label, fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    modifier = Modifier.testTag("nav_tab_dashboard")
                )

                NavigationBarItem(
                    selected = currentScreen == ShopScreen.BILLING,
                    onClick = { currentScreen = ShopScreen.BILLING },
                    icon = { Icon(Icons.Default.PointOfSale, contentDescription = "Billing") },
                    label = { Text(ShopScreen.BILLING.label, fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    modifier = Modifier.testTag("nav_tab_billing")
                )

                NavigationBarItem(
                    selected = currentScreen == ShopScreen.PRODUCTS,
                    onClick = { currentScreen = ShopScreen.PRODUCTS },
                    icon = { Icon(Icons.Default.Checkroom, contentDescription = "Inventory") },
                    label = { Text(ShopScreen.PRODUCTS.label, fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    modifier = Modifier.testTag("nav_tab_products")
                )

                NavigationBarItem(
                    selected = currentScreen == ShopScreen.HISTORY,
                    onClick = { currentScreen = ShopScreen.HISTORY },
                    icon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = "History") },
                    label = { Text(ShopScreen.HISTORY.label, fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    modifier = Modifier.testTag("nav_tab_history")
                )

                NavigationBarItem(
                    selected = currentScreen == ShopScreen.SETTINGS,
                    onClick = { currentScreen = ShopScreen.SETTINGS },
                    icon = { Icon(Icons.Default.Storefront, contentDescription = "Shop Settings") },
                    label = { Text(ShopScreen.SETTINGS.label, fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    modifier = Modifier.testTag("nav_tab_settings")
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        val contentModifier = Modifier.padding(innerPadding)

        when (currentScreen) {
            ShopScreen.DASHBOARD -> DashboardScreen(
                viewModel = viewModel,
                onNavigateToBilling = { currentScreen = ShopScreen.BILLING },
                onNavigateToProducts = { currentScreen = ShopScreen.PRODUCTS },
                onNavigateToHistory = { currentScreen = ShopScreen.HISTORY },
                onOpenAddProduct = { isAddingProductFromDashboard = true },
                modifier = contentModifier
            )
            ShopScreen.BILLING -> BillingScreen(
                viewModel = viewModel,
                snackbarHostState = snackbarHostState,
                modifier = contentModifier
            )
            ShopScreen.PRODUCTS -> ProductsScreen(
                viewModel = viewModel,
                modifier = contentModifier
            )
            ShopScreen.HISTORY -> BillHistoryScreen(
                viewModel = viewModel,
                modifier = contentModifier
            )
            ShopScreen.SETTINGS -> SettingsScreen(
                viewModel = viewModel,
                snackbarHostState = snackbarHostState,
                modifier = contentModifier
            )
        }

        // Active Receipt Dialog (when a bill is generated or selected)
        completedBill?.let { billWithItems ->
            BillReceiptDialog(
                billWithItems = billWithItems,
                shopProfile = shopProfile,
                onDismiss = { viewModel.clearCompletedBill() }
            )
        }

        // Add Product Dialog opened from Dashboard action
        if (isAddingProductFromDashboard) {
            AddEditProductDialog(
                initialProduct = null,
                onDismiss = { isAddingProductFromDashboard = false },
                onSave = { product ->
                    viewModel.addProduct(product)
                    isAddingProductFromDashboard = false
                }
            )
        }
    }
}
