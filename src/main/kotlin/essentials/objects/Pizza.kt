package essentials.objects.pizza

import org.junit.Test
import kotlin.test.assertEquals

class Pizza(
    val toppings: List<String>,
) {
    companion object PizzaFactory {
        fun hawaiian(): Pizza {
            return Pizza(listOf("ham", "pineapple"))
        }

        fun margherita(): Pizza {
            return Pizza(listOf("tomato", "mozzarella"))
        }
    }
}

//fun main() {
//val hawaiian = Pizza.hawaiian()
//println(hawaiian.toppings) // [ham, pineapple]
//val margherita = Pizza.margherita()
//println(margherita.toppings) // [tomato, mozzarella]
//}

class PizzaTest {
    @Test
    fun testHawaiian() {
        val expected = listOf("ham", "pineapple")
        assertEquals(expected, Pizza.hawaiian().toppings)
    }

    @Test
    fun testMargherita() {
        val expected = listOf("tomato", "mozzarella")
        assertEquals(expected, Pizza.margherita().toppings)
    }
}
