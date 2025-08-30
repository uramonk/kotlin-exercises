package essentials.classes.product

import org.junit.Test
import kotlin.test.assertEquals

class Product(var name: String, var price: Double, quantity: Int) {
    var quantity: Int = quantity
        set(value) {
            field = if (value < 0) 0 else value
        }

    fun calculateTotalValue(): Double {
        return price * quantity
    }

    fun restock(value: Int) {
        if (value >= 0) {
            quantity += value
        }
    }
}

//fun main() {
//val laptop = Product("Laptop", 999.99, 5)
//
//println(laptop.name) // Laptop
//println(laptop.quantity) // 5
//println(laptop.calculateTotalValue()) // 4999.95
//
//laptop.restock(3)
//
//println(laptop.quantity) // 8
//println(laptop.calculateTotalValue()) // 7999.92
//
//laptop.quantity = -2
//
//println(laptop.quantity) // 0
//println(laptop.calculateTotalValue()) // 0.0
//
//laptop.quantity = 10
//
//println(laptop.quantity) // 10
//println(laptop.calculateTotalValue()) // 9999.9
//}

class ProductTest {
    @Test
    fun `should keep name, price and quantity`() {
        val product = Product("Apple", 1.0, 10)
        assertEquals("Apple", product.name)
        assertEquals(1.0, product.price)
        assertEquals(10, product.quantity)
    }

    @Test
    fun `should calculate total value`() {
        assertEquals(10.0, Product("Apple", 1.0, 10).calculateTotalValue())
        assertEquals(0.0, Product("Apple", 1.0, 0).calculateTotalValue())
        assertEquals(0.0, Product("Apple", 0.0, 10).calculateTotalValue())
        assertEquals(4.8, Product("Apple", 1.2, 4).calculateTotalValue())
    }

    @Test
    fun `should restock`() {
        val product = Product("Apple", 1.0, 10)
        product.restock(5)
        assertEquals(15, product.quantity)
        product.restock(-5)
        assertEquals(15, product.quantity)
        product.restock(0)
        assertEquals(15, product.quantity)
    }

    @Test
    fun `should not allow negative quantity`() {
        val product = Product("Apple", 1.0, 10)
        product.quantity = -5
        assertEquals(0, product.quantity)
        product.quantity = 8
        assertEquals(8, product.quantity)
        product.quantity = 0
        assertEquals(0, product.quantity)
    }
}
