package idgenen

import org.apache.commons.lang3.RandomStringUtils
import kotlin.math.pow

private val list = mutableListOf<String>()

private var duplicates = 0

private var iteration = 0

fun main() {
    // No duplicates within 100 million iterations

    for (i in 1..10000000000) {
        val string = RandomStringUtils.random(4, true, true)

        if (list.contains(string)) {
            duplicates++
            println("Duplicate found: $string Duplicates so far: $duplicates Iterations: $iteration")
        } else {
            println("No duplicate: $string Duplicates so far: $duplicates Iterations: $iteration")
        }

        iteration++
    }

    println()
    println()
    println("Duplicates: $duplicates")
    println("Iterations: $iteration")
}