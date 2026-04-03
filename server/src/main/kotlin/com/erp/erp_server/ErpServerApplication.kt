package com.erp.erp_server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ErpServerApplication

fun main(args: Array<String>) {
    runApplication<ErpServerApplication>(*args)
}