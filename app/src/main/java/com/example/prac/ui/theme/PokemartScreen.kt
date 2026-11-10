package com.example.prac.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun PokemartApp(modifier: Modifier = Modifier) {
    val quantities = remember { mutableStateMapOf<String, Int>() }
    val products = ProductCatalog.products

    var showSuccessScreen by remember { mutableStateOf(false) }

    val totalItems = products.sumOf { quantities[it.id] ?: 0 }
    val subtotal = products.sumOf { it.price * (quantities[it.id] ?: 0) }
    val discount = if (subtotal >= 150.0) subtotal * 0.10 else 0.0
    val afterDiscount = subtotal - discount
    val shipping = if (totalItems > 0 && afterDiscount >= 120.0) 0.0 else if (totalItems > 0) 15.0 else 0.0
    val total = if (totalItems > 0) afterDiscount + shipping else 0.0

    val summary = CartSummary(
        totalItems = totalItems,
        subtotal = subtotal,
        discount = discount,
        shipping = shipping,
        total = total
    )

    if (showSuccessScreen) {
        val purchasedItems = products.filter { (quantities[it.id] ?: 0) > 0 }
            .map { product -> product to (quantities[product.id] ?: 0) }

        SuccessScreen(
            summary = summary,
            purchasedItems = purchasedItems,
            onBackToStore = {
                quantities.clear()
                showSuccessScreen = false
            },
            modifier = modifier
        )
    } else {
        PokemartScreenContent(
            products = products,
            quantities = quantities,
            summary = summary,
            onIncrement = { id ->
                val current = quantities[id] ?: 0
                if (current < 10) quantities[id] = current + 1
            },
            onDecrement = { id ->
                val current = quantities[id] ?: 0
                if (current > 0) quantities[id] = current - 1
            },
            onClear = { quantities.clear() },
            onConfirm = { showSuccessScreen = true },
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemartScreenContent(
    products: List<Product>,
    quantities: Map<String, Int>,
    summary: CartSummary,
    onIncrement: (String) -> Unit,
    onDecrement: (String) -> Unit,
    onClear: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Pokémart") },
                navigationIcon = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                actions = {
                    Icon(
                        Icons.Filled.ShoppingCart,
                        contentDescription = "Carrito",
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            )
        },
        bottomBar = {
            OrderSummary(
                summary = summary,
                onClear = onClear,
                onConfirm = onConfirm,
                modifier = Modifier.padding(12.dp).navigationBarsPadding()
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            items(products, key = { it.id }) { product ->
                val qty = quantities[product.id] ?: 0
                ProductRow(
                    product = product,
                    quantity = qty,
                    onIncrement = { onIncrement(product.id) },
                    onDecrement = { onDecrement(product.id) }
                )
                HorizontalDivider()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuccessScreen(
    summary: CartSummary,
    purchasedItems: List<Pair<Product, Int>>,
    onBackToStore: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Detalle de Compra") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(72.dp)
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "¡Compra Exitosa!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Gracias por tu pedido en Pokémart",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = "Artículos adquiridos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    purchasedItems.forEach { (product, quantity) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${product.name} x$quantity", style = MaterialTheme.typography.bodyMedium)
                            Text(money(product.price * quantity), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        }
                    }

                    HorizontalDivider(Modifier.padding(vertical = 8.dp))

                    SummaryRow("Subtotal", money(summary.subtotal))
                    if (summary.discount > 0) {
                        SummaryRow("Descuento (10 %)", "-${money(summary.discount)}")
                    }
                    SummaryRow("Envío", if (summary.shipping == 0.0) "Gratis" else money(summary.shipping))

                    HorizontalDivider(Modifier.padding(vertical = 8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Pagado", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(money(summary.total), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = onBackToStore,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Volver a la tienda")
            }
        }
    }
}

@Composable
fun ProductRow(
    product: Product,
    quantity: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    ListItem(
        modifier = modifier,
        leadingContent = {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(product.initials, style = MaterialTheme.typography.labelLarge)
                }
            }
        },
        headlineContent = { Text(product.name, fontWeight = FontWeight.Medium) },
        supportingContent = { Text(money(product.price)) },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FilledTonalIconButton(onClick = onDecrement, enabled = quantity > 0, modifier = Modifier.size(32.dp)) {
                    Text("−")
                }
                Text(
                    text = quantity.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(min = 32.dp)
                )
                FilledIconButton(onClick = onIncrement, enabled = quantity < 10, modifier = Modifier.size(32.dp)) {
                    Text("+")
                }
            }
        }
    )
}

@Composable
fun OrderSummary(
    summary: CartSummary,
    onClear: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.large
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            SummaryRow("Artículos", summary.totalItems.toString())
            SummaryRow("Subtotal", money(summary.subtotal))
            SummaryRow(
                "Descuento (10 %)",
                if (summary.discount > 0) "-${money(summary.discount)}" else money(0.0)
            )
            SummaryRow(
                "Envío",
                if (!summary.isEmpty && summary.shipping == 0.0) "Gratis" else money(summary.shipping)
            )
            HorizontalDivider(Modifier.padding(vertical = 4.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Total", style = MaterialTheme.typography.titleMedium)
                Text(
                    money(summary.total),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onClear, modifier = Modifier.weight(1f)) {
                    Text("Vaciar carrito")
                }
                Button(
                    onClick = onConfirm,
                    enabled = !summary.isEmpty,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Confirmar")
                }
            }
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

private fun money(value: Double) = "$%.2f".format(value)

@Preview(showBackground = true, name = "Vista Previa App")
@Composable
fun PokemartAppPreview() {
    PokemartTheme {
        PokemartApp()
    }
}