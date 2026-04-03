package com.erp.erp_server

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.bind.annotation.RestController

data class UserSummaryResponse(
    val id: Long,
    val username: String,
    val userType: String
)

@RestController
class UserController(
    private val userCredentialRepository: UserCredentialRepository,
    private val authService: AuthService
) {
    @GetMapping("/api/users")
    fun users(@RequestParam username: String): List<UserSummaryResponse> {
        val sanitizedUsername = username.trim()
        if (sanitizedUsername.isEmpty()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing username")
        }

        val project = authService.findProjectByUsername(sanitizedUsername)
            ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "Unknown user")

        return userCredentialRepository.findAllByProject(project)
            .sortedBy { it.id }
            .map { user ->
                UserSummaryResponse(
                    id = user.id ?: 0L,
                    username = user.username,
                    userType = user.userType.lowercase()
                )
            }
    }
}