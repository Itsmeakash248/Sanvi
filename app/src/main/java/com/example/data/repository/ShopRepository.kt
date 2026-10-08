package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.database.AppDatabase
import com.example.data.model.BillEntity
import com.example.data.model.BillItemEntity
import com.example.data.model.BillWithItems
import com.example.data.model.ProductEntity
import kotlinx.coroutines.flow.Flow

class ShopRepository(private val database: AppDatabase) {
    private val productDao = database.productDao()
    private val billDao = database.billDao()

    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()
    val lowStockProducts: Flow<List<ProductEntity>> = productDao.getLowStockProducts(threshold = 5)
    val allBills: Flow<List<BillWithItems>> = billDao.getAllBillsWithItems()

    suspend fun insertProduct(product: ProductEntity): Long {
        return productDao.insertProduct(product)
    }

    suspend fun updateProduct(product: ProductEntity) {
        productDao.updateProduct(product)
    }

    suspend fun deleteProduct(product: ProductEntity) {
        productDao.deleteProduct(product)
    }

    suspend fun updateStock(productId: Long, newStock: Int) {
        productDao.updateStock(productId, newStock)
    }

    suspend fun adjustStock(productId: Long, delta: Int) {
        productDao.adjustStock(productId, delta)
    }

    suspend fun createSaleBill(
        bill: BillEntity,
        items: List<BillItemEntity>
    ): Long {
        return database.withTransaction {
            val generatedBillId = billDao.insertBill(bill)
            val mappedItems = items.map { it.copy(billId = generatedBillId) }
            billDao.insertBillItems(mappedItems)

            // Deduct stock for each sold item
            for (item in items) {
                productDao.adjustStock(item.productId, -item.quantity)
            }

            generatedBillId
        }
    }

    suspend fun deleteBill(billId: Long) {
        billDao.deleteBill(billId)
    }

    suspend fun seedInitialDataIfEmpty() {
        if (productDao.countProducts() == 0) {
            val initialProducts = listOf(
                ProductEntity(
                    name = "Chanderi Silk Embroidered Kurti",
                    category = "Kurtis",
                    size = "M",
                    color = "Maroon",
                    sellingPrice = 1499.0,
                    costPrice = 850.0,
                    stockQuantity = 8,
                    barcode = "SCS-KUR-01",
                    description = "Pure Chanderi silk kurti with intricate zardozi neckwork."
                ),
                ProductEntity(
                    name = "Cotton Printed Straight Kurta",
                    category = "Kurtis",
                    size = "L",
                    color = "Indigo Blue",
                    sellingPrice = 699.0,
                    costPrice = 380.0,
                    stockQuantity = 14,
                    barcode = "SCS-KUR-02",
                    description = "100% breathable daily-wear Jaipur block print cotton kurta."
                ),
                ProductEntity(
                    name = "Banarasi Georgette Festive Saree",
                    category = "Sarees",
                    size = "Free Size",
                    color = "Emerald Green",
                    sellingPrice = 2499.0,
                    costPrice = 1500.0,
                    stockQuantity = 3, // Low stock
                    barcode = "SCS-SAR-01",
                    description = "Rich zari woven border with running designer blouse piece."
                ),
                ProductEntity(
                    name = "Kanjivaram Soft Silk Saree",
                    category = "Sarees",
                    size = "Free Size",
                    color = "Ruby Pink",
                    sellingPrice = 3299.0,
                    costPrice = 2100.0,
                    stockQuantity = 6,
                    barcode = "SCS-SAR-02",
                    description = "Traditional wedding silk saree with antique golden pallu."
                ),
                ProductEntity(
                    name = "Men's Slim Fit Oxford Shirt",
                    category = "Shirts",
                    size = "40 (L)",
                    color = "Pure White",
                    sellingPrice = 899.0,
                    costPrice = 480.0,
                    stockQuantity = 12,
                    barcode = "SCS-SHT-01",
                    description = "Crisp formal and party-ready cotton Oxford button-down shirt."
                ),
                ProductEntity(
                    name = "Linen Mandarin Collar Casual Shirt",
                    category = "Shirts",
                    size = "42 (XL)",
                    color = "Sand Beige",
                    sellingPrice = 1199.0,
                    costPrice = 650.0,
                    stockQuantity = 2, // Low stock
                    barcode = "SCS-SHT-02",
                    description = "Lightweight pure linen summer shirt with Chinese collar."
                ),
                ProductEntity(
                    name = "Women's High-Rise Ankle Denim Jeans",
                    category = "Jeans",
                    size = "30",
                    color = "Light Sky Blue",
                    sellingPrice = 1299.0,
                    costPrice = 750.0,
                    stockQuantity = 11,
                    barcode = "SCS-JNS-01",
                    description = "4-way stretch denim with flattering contour fit."
                ),
                ProductEntity(
                    name = "Men's Regular Straight Denim Jeans",
                    category = "Jeans",
                    size = "32",
                    color = "Dark Charcoal",
                    sellingPrice = 1499.0,
                    costPrice = 850.0,
                    stockQuantity = 7,
                    barcode = "SCS-JNS-02",
                    description = "Heavyweight durable cotton denim with reinforced stitching."
                ),
                ProductEntity(
                    name = "Floral Tiered Bohemian Maxi Dress",
                    category = "Dresses",
                    size = "S",
                    color = "Peach Pink",
                    sellingPrice = 1099.0,
                    costPrice = 620.0,
                    stockQuantity = 4, // Low stock
                    barcode = "SCS-DRS-01",
                    description = "Comfortable georgette tiered summer dress with belt."
                ),
                ProductEntity(
                    name = "Kids Festive Kurta Pajama Set",
                    category = "Kids",
                    size = "26 (6-7Y)",
                    color = "Mustard Gold",
                    sellingPrice = 799.0,
                    costPrice = 420.0,
                    stockQuantity = 9,
                    barcode = "SCS-KID-01",
                    description = "Gentle soft jacquard silk kurta with cotton lining and pajama."
                ),
                ProductEntity(
                    name = "Rayon Flared Palazzo Bottom",
                    category = "Bottoms",
                    size = "Free Size",
                    color = "Off White",
                    sellingPrice = 449.0,
                    costPrice = 240.0,
                    stockQuantity = 18,
                    barcode = "SCS-BTM-01",
                    description = "Elasticated waistband with lace border accents."
                ),
                ProductEntity(
                    name = "Handcrafted Phulkari Dupatta",
                    category = "Accessories",
                    size = "Free Size",
                    color = "Multi Red",
                    sellingPrice = 499.0,
                    costPrice = 270.0,
                    stockQuantity = 1, // Low stock
                    barcode = "SCS-ACC-01",
                    description = "Vibrant traditional embroidery work with golden fringe lace."
                )
            )
            productDao.insertProducts(initialProducts)
        }
    }
}
