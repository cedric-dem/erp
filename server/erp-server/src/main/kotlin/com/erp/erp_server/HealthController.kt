package com.erp.erp_server

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

data class UserSummaryResponse(
    val id: Long,
    val username: String,
    val userType: String
)

@RestController
class HealthController(
    private val userCredentialRepository: UserCredentialRepository
) {
    @GetMapping("/api/health")
    fun health(): Map<String, String> = mapOf(
        "status" to "UP",
        "service" to "erp-server"
    )

    @GetMapping("/api/users")
    fun users(): List<UserSummaryResponse> = userCredentialRepository.findAll()
        .sortedBy { it.id }
        .map { user ->
            UserSummaryResponse(
                id = user.id ?: 0L,
                username = user.username,
                userType = user.userType.lowercase()
            )
        }
}