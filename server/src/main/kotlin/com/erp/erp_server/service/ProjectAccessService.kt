package com.erp.erp_server

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class ProjectAccessService(
    private val authService: AuthService
) {
    fun resolveProjectOrThrow(username: String): String {
        val sanitizedUsername = username.trim()
        if (sanitizedUsername.isEmpty()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing username")
        }

        return authService.findProjectByUsername(sanitizedUsername)
            ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "Unknown user")
    }

    fun sanitizeDisplayName(username: String): String = username.trim().ifEmpty { "User" }
}