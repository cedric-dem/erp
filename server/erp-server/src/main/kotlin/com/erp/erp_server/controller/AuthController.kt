package com.erp.erp_server

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class AuthRequest(
    val username: String = "",
    val password: String = ""
)

data class AuthResponse(
    val message: String
)

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {
    @PostMapping("/login")
    fun login(@RequestBody request: AuthRequest): ResponseEntity<AuthResponse> {
        val username = request.username.trim()
        val password = request.password.trim()

        if (username.isEmpty() || password.isEmpty()) {
            return ResponseEntity.badRequest().body(AuthResponse("Please enter a username and password."))
        }

        val validUser = authService.isValidLogin(username, password)
        if (!validUser) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(AuthResponse("Invalid credentials."))
        }

        return ResponseEntity.ok(AuthResponse("Login successful. Welcome $username!"))
    }

    @PostMapping("/register")
    fun register(@RequestBody request: AuthRequest): ResponseEntity<AuthResponse> {
        val username = request.username.trim()
        val password = request.password.trim()

        if (username.isEmpty() || password.isEmpty()) {
            return ResponseEntity.badRequest().body(AuthResponse("Please choose a username and password."))
        }

        val registered = authService.register(username, password)
        if (!registered) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(AuthResponse("This username already exists."))
        }

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(AuthResponse("Account created successfully. You can now sign in."))
    }
}