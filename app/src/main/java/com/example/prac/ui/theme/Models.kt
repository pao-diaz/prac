package com.example.prac.ui.theme

data class Product(
    val id: String,
    val initials: String,
    val name: String,
    val price: Double
)

data class CartSummary(
    val totalItems: Int = 0,
    val subtotal: Double = 0.0,
    val discount: Double = 0.0,
    val shipping: Double = 0.0,
    val total: Double = 0.0
) {
    val isEmpty: Boolean get() = totalItems == 0
}

object ProductCatalog {
    val products = listOf(
        Product("pocion", "Po", "Poción", 20.00),
        Product("pokebola", "Pk", "Pokébola", 25.00),
        Product("antidoto", "An", "Antídoto", 12.50),
        Product("revivir", "Re", "Revivir", 60.00)
    )
}

object CartCalculator {
    fun calculate(products: List<Product>, quantities: Map<String, Int>): CartSummary {
        val totalItems = products.sumOf { quantities[it.id] ?: 0 }
        if (totalItems == 0) return CartSummary()

        val subtotal = products.sumOf { it.price * (quantities[it.id] ?: 0) }
        val discount = if (subtotal >= 150.0) subtotal * 0.10 else 0.0
        val afterDiscount = subtotal - discount
        val shipping = if (afterDiscount >= 120.0) 0.0 else 15.0

        return CartSummary(totalItems, subtotal, discount, shipping, afterDiscount + shipping)
    }
}