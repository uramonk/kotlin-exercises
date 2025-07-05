package essentials.exceptions.handleexceptions

import java.lang.ArithmeticException
import java.lang.NumberFormatException

fun main() {
    while (true) {
        // Wrap below function call with try-catching block,
        // and handle possible exceptions.
        try {
            handleInput()
        } catch (e: NumberFormatException) {
            println("数字を入力してください")
        } catch (e: IllegalOperatorException) {
            println("+, -, *, / を入力してください")
        } catch (e: ArithmeticException) {
            println("0で割っている")
        }
    }
}

fun handleInput() {
    print("Enter the first number: ")
    val num1 = readln().toInt()
    print("Enter an operator (+, -, *, /): ")
    val operator = readln()
    print("Enter the second number: ")
    val num2 = readln().toInt()

    val result = when (operator) {
        "+" -> num1 + num2
        "-" -> num1 - num2
        "*" -> num1 * num2
        "/" -> num1 / num2
        else -> throw IllegalOperatorException(operator)
    }

    println("Result: $result")
}

class IllegalOperatorException(val operator: String) :
    Exception("Unknown operator: $operator")
