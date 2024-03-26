package com.restaurant.be

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.restaurant.be"])
class BeApplication

fun main(args: Array<String>) {
	runApplication<BeApplication>(*args)
}
