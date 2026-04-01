package com.erp.erp_server

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HealthController {
    @GetMapping("/api/health")
    fun health(): Map<String, String> = mapOf(
        "status" to "UP",
        "service" to "erp-server"
    )

    @GetMapping("/users")
    fun users(): List<Map<String, String>> = listOf(
        mapOf("id" to "1", "name" to "Alice"),
        mapOf("id" to "2", "name" to "Bob")
    )
}