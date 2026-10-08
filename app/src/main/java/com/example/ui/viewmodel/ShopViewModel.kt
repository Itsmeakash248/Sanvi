package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.BillEntity
import com.example.data.model.BillItemEntity
import com.example.data.model.BillWithItems
import com.example.data.model.ProductEntity
import com.example.data.repository.ShopRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class UserRole {
    OWNER,
    STAFF
}

enum class StockFilter {
    ALL,
    LOW_STOCK,
    OUT_OF_STOCK
}

enum class BillDateFilter {
    TODAY,
    YESTERDAY,
    THIS_WEEK,
    THIS_MONTH,
    ALL
}

data class CartItem(
    val product: ProductEntity,
    val quantity: Int
) {
    val total: Double get() = product.sellingPrice * quantity
}

data class ShopProfile(
    val name: String = "Sanvi Clothing Shop",
    val tagline: String = "Trendy & Ethnic Wear Boutique",
    val address: String = "Shop #14, Royal Complex, Station Road",
    val phone: String = "+91 98765 43210",
    val upiId: String = "sanviclothing@upi",
    val gstNumber: String = "27AAACS1234F1Z8"
)

class ShopViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ShopRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ShopRepository(database)
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    // Role state
    private val _userRole = MutableStateFlow(UserRole.OWNER)
    val userRole: StateFlow<UserRole> = _userRole.asStateFlow()

    fun switchRole(role: UserRole) {
        _userRole.value = role
    }

    // Shop Profile
    private val _shopProfile = MutableStateFlow(ShopProfile())
    val shopProfile: StateFlow<ShopProfile> = _shopProfile.asStateFlow()

    fun updateShopProfile(profile: ShopProfile) {
        _shopProfile.value = profile
    }

    // Products
    val allProducts: StateFlow<List<ProductEntity>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockProducts: StateFlow<List<ProductEntity>> = repository.lowStockProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Product search and filter
    val productSearchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow("All")
    val stockFilter = MutableStateFlow(StockFilter.ALL)

    val filteredProducts: StateFlow<List<ProductEntity>> = combine(
        allProducts,
        productSearchQuery,
        selectedCategory,
        stockFilter
    ) { products, query, category, filter ->
        products.filter { product ->
            val matchesQuery = query.isBlank() ||
                    product.name.contains(query, ignoreCase = true) ||
                    product.category.contains(query, ignoreCase = true) ||
                    product.size.contains(query, ignoreCase = true) ||
                    product.color.contains(query, ignoreCase = true) ||
                    product.barcode.contains(query, ignoreCase = true)

            val matchesCategory = category == "All" || product.category.equals(category, ignoreCase = true)

            val matchesStock = when (filter) {
                StockFilter.ALL -> true
                StockFilter.LOW_STOCK -> product.stockQuantity in 1..5
                StockFilter.OUT_OF_STOCK -> product.stockQuantity <= 0
            }

            matchesQuery && matchesCategory && matchesStock
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Billing / Cart State
    private val _cart = MutableStateFlow<Map<Long, Int>>(emptyMap())
    val cart: StateFlow<Map<Long, Int>> = _cart.asStateFlow()

    val customerName = MutableStateFlow("")
    val customerPhone = MutableStateFlow("")
    val discountPercent = MutableStateFlow(0.0) // discount percentage e.g. 5.0
    val taxRate = MutableStateFlow(0.0) // GST 0%, 5%, 12%
    val paymentMethod = MutableStateFlow("CASH") // CASH, UPI, CARD
    val billNotes = MutableStateFlow("")

    // Active receipt dialog
    private val _completedBill = MutableStateFlow<BillWithItems?>(null)
    val completedBill: StateFlow<BillWithItems?> = _completedBill.asStateFlow()

    fun clearCompletedBill() {
        _completedBill.value = null
    }

    fun showBillReceipt(bill: BillWithItems) {
        _completedBill.value = bill
    }

    // Cart operations
    fun addToCart(product: ProductEntity) {
        if (product.stockQuantity <= 0) return
        val currentQty = _cart.value[product.id] ?: 0
        if (currentQty < product.stockQuantity) {
            _cart.value = _cart.value + (product.id to currentQty + 1)
        }
    }

    fun removeFromCart(productId: Long) {
        val currentQty = _cart.value[productId] ?: return
        if (currentQty > 1) {
            _cart.value = _cart.value + (productId to currentQty - 1)
        } else {
            _cart.value = _cart.value - productId
        }
    }

    fun deleteItemFromCart(productId: Long) {
        _cart.value = _cart.value - productId
    }

    fun clearCart() {
        _cart.value = emptyMap()
        customerName.value = ""
        customerPhone.value = ""
        discountPercent.value = 0.0
        taxRate.value = 0.0
        paymentMethod.value = "CASH"
        billNotes.value = ""
    }

    // Bills History
    val allBills: StateFlow<List<BillWithItems>> = repository.allBills
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val billSearchQuery = MutableStateFlow("")
    val billDateFilter = MutableStateFlow(BillDateFilter.TODAY)

    val filteredBills: StateFlow<List<BillWithItems>> = combine(
        allBills,
        billSearchQuery,
        billDateFilter
    ) { bills, query, dateFilter ->
        val now = Calendar.getInstance()
        val startOfToday = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val startOfYesterday = Calendar.getInstance().apply {
            timeInMillis = startOfToday
            add(Calendar.DAY_OF_YEAR, -1)
        }.timeInMillis

        val startOfWeek = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val startOfMonth = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        bills.filter { billWithItems ->
            val b = billWithItems.bill
            val matchesQuery = query.isBlank() ||
                    b.invoiceNumber.contains(query, ignoreCase = true) ||
                    b.customerName.contains(query, ignoreCase = true) ||
                    b.customerPhone.contains(query, ignoreCase = true)

            val matchesDate = when (dateFilter) {
                BillDateFilter.TODAY -> b.timestamp >= startOfToday
                BillDateFilter.YESTERDAY -> b.timestamp in startOfYesterday until startOfToday
                BillDateFilter.THIS_WEEK -> b.timestamp >= startOfWeek
                BillDateFilter.THIS_MONTH -> b.timestamp >= startOfMonth
                BillDateFilter.ALL -> true
            }

            matchesQuery && matchesDate
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Product actions
    fun addProduct(product: ProductEntity, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.insertProduct(product)
            onComplete()
        }
    }

    fun updateProduct(product: ProductEntity, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateProduct(product)
            onComplete()
        }
    }

    fun deleteProduct(product: ProductEntity, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            onComplete()
        }
    }

    fun adjustStock(productId: Long, delta: Int) {
        viewModelScope.launch {
            repository.adjustStock(productId, delta)
        }
    }

    fun setStock(productId: Long, newStock: Int) {
        viewModelScope.launch {
            repository.updateStock(productId, newStock.coerceAtLeast(0))
        }
    }

    // Complete Sale / Billing
    fun completeSale(onSuccess: (BillWithItems) -> Unit, onError: (String) -> Unit) {
        val cartMap = _cart.value
        if (cartMap.isEmpty()) {
            onError("Cart is empty! Please select at least one item.")
            return
        }

        val productsMap = allProducts.value.associateBy { it.id }
        val billItems = mutableListOf<BillItemEntity>()
        var subtotal = 0.0

        for ((productId, qty) in cartMap) {
            val product = productsMap[productId]
            if (product == null) {
                onError("Product not found.")
                return
            }
            if (qty > product.stockQuantity) {
                onError("Stock for '${product.name}' is only ${product.stockQuantity} pcs!")
                return
            }

            val itemTotal = product.sellingPrice * qty
            subtotal += itemTotal
            billItems.add(
                BillItemEntity(
                    billId = 0,
                    productId = product.id,
                    productName = product.name,
                    category = product.category,
                    size = product.size,
                    color = product.color,
                    quantity = qty,
                    unitPrice = product.sellingPrice,
                    totalPrice = itemTotal
                )
            )
        }

        val discountVal = discountPercent.value
        val discountAmount = (subtotal * discountVal / 100.0)
        val discountedSubtotal = (subtotal - discountAmount).coerceAtLeast(0.0)
        val taxRateVal = taxRate.value
        val taxAmount = (discountedSubtotal * taxRateVal / 100.0)
        val grandTotal = (discountedSubtotal + taxAmount)

        val invoiceNum = generateInvoiceNumber()

        val billEntity = BillEntity(
            invoiceNumber = invoiceNum,
            timestamp = System.currentTimeMillis(),
            customerName = customerName.value.trim().ifEmpty { "Cash Customer" },
            customerPhone = customerPhone.value.trim(),
            subtotal = subtotal,
            discountType = "PERCENT",
            discountValue = discountVal,
            discountAmount = discountAmount,
            taxRate = taxRateVal,
            taxAmount = taxAmount,
            grandTotal = grandTotal,
            paymentMethod = paymentMethod.value,
            notes = billNotes.value.trim()
        )

        viewModelScope.launch {
            try {
                val billId = repository.createSaleBill(billEntity, billItems)
                val finalizedBill = BillWithItems(
                    bill = billEntity.copy(billId = billId),
                    items = billItems.map { it.copy(billId = billId) }
                )
                clearCart()
                _completedBill.value = finalizedBill
                onSuccess(finalizedBill)
            } catch (e: Exception) {
                onError("Error creating bill: ${e.localizedMessage}")
            }
        }
    }

    private fun generateInvoiceNumber(): String {
        val dateFormat = SimpleDateFormat("yyMMdd-HHmm", Locale.getDefault())
        val randomSuffix = (10..99).random()
        return "SCS-${dateFormat.format(Date())}-$randomSuffix"
    }

    fun deleteBill(billId: Long) {
        viewModelScope.launch {
            repository.deleteBill(billId)
        }
    }
}
