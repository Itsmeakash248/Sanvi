package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.ProductEntity

val CATEGORY_OPTIONS = listOf(
    "Kurtis", "Sarees", "Shirts", "Jeans", "Dresses", "Kids", "Bottoms", "Accessories", "T-Shirts"
)

val SIZE_OPTIONS = listOf(
    "S", "M", "L", "XL", "XXL", "Free Size", "28", "30", "32", "34", "36"
)

val COLOR_OPTIONS = listOf(
    "Maroon", "Navy Blue", "Black", "White", "Ruby Pink", "Emerald Green", "Mustard Gold", "Beige", "Sky Blue"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditProductDialog(
    initialProduct: ProductEntity? = null,
    onDismiss: () -> Unit,
    onSave: (ProductEntity) -> Unit
) {
    var name by remember { mutableStateOf(initialProduct?.name ?: "") }
    var selectedCategory by remember { mutableStateOf(initialProduct?.category ?: "Kurtis") }
    var selectedSize by remember { mutableStateOf(initialProduct?.size ?: "M") }
    var selectedColor by remember { mutableStateOf(initialProduct?.color ?: "Navy Blue") }
    var sellingPriceStr by remember { mutableStateOf(initialProduct?.sellingPrice?.let { if (it > 0) it.toInt().toString() else "" } ?: "") }
    var costPriceStr by remember { mutableStateOf(initialProduct?.costPrice?.let { if (it > 0) it.toInt().toString() else "" } ?: "") }
    var stockQuantityStr by remember { mutableStateOf(initialProduct?.stockQuantity?.toString() ?: "10") }
    var barcode by remember { mutableStateOf(initialProduct?.barcode ?: "") }
    var description by remember { mutableStateOf(initialProduct?.description ?: "") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = if (initialProduct == null) "Add Clothing Product" else "Edit Clothing Product",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Product Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; errorMessage = null },
                    label = { Text("Product Name *") },
                    placeholder = { Text("e.g. Cotton Printed Kurti") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("product_name_input")
                )

                // Category Selection
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Category *",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        CATEGORY_OPTIONS.forEach { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }
                }

                // Size Selection
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Size *",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        SIZE_OPTIONS.forEach { sz ->
                            FilterChip(
                                selected = selectedSize == sz,
                                onClick = { selectedSize = sz },
                                label = { Text(sz) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            )
                        }
                    }
                }

                // Color Selection
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Colour *",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        COLOR_OPTIONS.forEach { col ->
                            FilterChip(
                                selected = selectedColor == col,
                                onClick = { selectedColor = col },
                                label = { Text(col) }
                            )
                        }
                    }
                }

                // Prices: Selling Price & Cost Price
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = sellingPriceStr,
                        onValueChange = { sellingPriceStr = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Selling Price (₹) *") },
                        placeholder = { Text("999") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("selling_price_input")
                    )

                    OutlinedTextField(
                        value = costPriceStr,
                        onValueChange = { costPriceStr = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Cost Price (₹)") },
                        placeholder = { Text("600") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("cost_price_input")
                    )
                }

                // Stock Quantity & SKU/Barcode
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = stockQuantityStr,
                        onValueChange = { stockQuantityStr = it.filter { c -> c.isDigit() } },
                        label = { Text("Available Stock (Pcs) *") },
                        placeholder = { Text("15") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("stock_quantity_input")
                    )

                    OutlinedTextField(
                        value = barcode,
                        onValueChange = { barcode = it },
                        label = { Text("SKU / Tag") },
                        placeholder = { Text("SCS-01") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("barcode_input")
                    )
                }

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Fabric / Style Notes") },
                    placeholder = { Text("e.g. Pure cotton, machine wash") },
                    maxLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("description_input")
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Actions: Cancel & Save
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = {
                            val price = sellingPriceStr.toDoubleOrNull()
                            val stock = stockQuantityStr.toIntOrNull()
                            val cost = costPriceStr.toDoubleOrNull() ?: 0.0

                            if (name.isBlank()) {
                                errorMessage = "Product name cannot be empty."
                                return@Button
                            }
                            if (price == null || price <= 0.0) {
                                errorMessage = "Please enter a valid selling price."
                                return@Button
                            }
                            if (stock == null || stock < 0) {
                                errorMessage = "Please enter valid stock quantity."
                                return@Button
                            }

                            val product = ProductEntity(
                                id = initialProduct?.id ?: 0,
                                name = name.trim(),
                                category = selectedCategory,
                                size = selectedSize,
                                color = selectedColor,
                                sellingPrice = price,
                                costPrice = cost,
                                stockQuantity = stock,
                                barcode = barcode.trim(),
                                description = description.trim(),
                                createdAt = initialProduct?.createdAt ?: System.currentTimeMillis()
                            )
                            onSave(product)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("save_product_button")
                    ) {
                        Text(if (initialProduct == null) "Add Product" else "Update Product")
                    }
                }
            }
        }
    }
}
