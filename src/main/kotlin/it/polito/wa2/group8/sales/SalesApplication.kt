package it.polito.wa2.group8.sales

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
class SalesApplication

fun main(args: Array<String>) {
	runApplication<SalesApplication>(*args)
}
