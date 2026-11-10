package com.example.prac.ui.theme

import androidx.compose.runtime.mutableStateMapOf

class CartState {
    companion object { const val MAX_QUANTITY = 10 }

    val quantities = mutableStateMapOf<String, Int>()

    fun quantityOf(id: String): Int = quantities[id] ?: 0

    fun increment(id: String) {
        if (quantityOf(id) < MAX_QUANTITY) quantities[id] = quantityOf(id) + 1
    }

    fun decrement(id: String) {
        if (quantityOf(id) > 0) quantities[id] = quantityOf(id) - 1
    }

    fun clear() = quantities.clear()
}