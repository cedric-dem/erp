package com.erp.erp_server

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class AuthRequest(
    val username: String? = null,
    val password: String? = null,
    val project: String? = null,
    val projectAction: String? = null
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
        val username = request.username.orEmpty().trim()
        val password = request.password.orEmpty().trim()

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
        val username = request.username.orEmpty().trim()
        val password = request.password.orEmpty().trim()
        val project = request.project.orEmpty().trim()
        val projectAction = request.projectAction.orEmpty().trim().ifEmpty { "join" }

        if (username.isEmpty() || password.isEmpty() || project.isEmpty()) {
            return ResponseEntity.badRequest().body(AuthResponse("Please choose a username, password, and project."))
        }

        return when (authService.register(username, password, project, projectAction)) {
            AuthService.RegisterResult.SUCCESS -> ResponseEntity.status(HttpStatus.CREATED)
                .body(AuthResponse("Account created successfully. You can now sign in."))

            AuthService.RegisterResult.USERNAME_EXISTS -> ResponseEntity.status(HttpStatus.CONFLICT)
                .body(AuthResponse("This username already exists."))

            AuthService.RegisterResult.PROJECT_EXISTS -> ResponseEntity.status(HttpStatus.CONFLICT)
                .body(AuthResponse("This project already exists. Please join it or choose another name."))

            AuthService.RegisterResult.PROJECT_NOT_FOUND -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(AuthResponse("Project not found. Please choose an existing project or create a new one."))

            AuthService.RegisterResult.INVALID_PROJECT_ACTION -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(AuthResponse("Invalid project action. Please choose to join an existing project or create a new one."))

            AuthService.RegisterResult.SAVE_FAILED -> ResponseEntity.status(HttpStatus.CONFLICT)
                .body(AuthResponse("Unable to create account. Please try a different username."))
        }
    }
}