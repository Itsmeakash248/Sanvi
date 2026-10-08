package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.AppDatabase
import com.example.data.model.BillEntity
import com.example.data.model.BillItemEntity
import com.example.data.model.ProductEntity
import com.example.data.repository.ShopRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Sanvi Clothing", appName)
  }

  @Test
  fun `verify database seeding and sale transaction`() = runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val database = AppDatabase.getDatabase(context)
    val repository = ShopRepository(database)

    repository.seedInitialDataIfEmpty()
    val products = repository.allProducts.first()
    assertEquals(true, products.isNotEmpty())

    val firstProduct = products.first()
    val initialStock = firstProduct.stockQuantity

    // Test a sale bill
    val bill = BillEntity(
      invoiceNumber = "TEST-001",
      customerName = "Pooja",
      subtotal = firstProduct.sellingPrice,
      grandTotal = firstProduct.sellingPrice
    )
    val item = BillItemEntity(
      billId = 0,
      productId = firstProduct.id,
      productName = firstProduct.name,
      category = firstProduct.category,
      size = firstProduct.size,
      color = firstProduct.color,
      quantity = 1,
      unitPrice = firstProduct.sellingPrice,
      totalPrice = firstProduct.sellingPrice
    )

    val billId = repository.createSaleBill(bill, listOf(item))
    assertEquals(true, billId > 0)

    // Stock should be deducted by 1
    val updatedProducts = repository.allProducts.first()
    val updatedItem = updatedProducts.first { it.id == firstProduct.id }
    assertEquals(initialStock - 1, updatedItem.stockQuantity)
  }
}
