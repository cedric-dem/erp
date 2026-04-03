package com.erp.erp_server

import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val repository: UserCredentialRepository
) {
    fun isValidLogin(username: String, password: String): Boolean {
        return repository.existsByUsernameAndPassword(username, password)
    }

    fun register(username: String, password: String): Boolean {
        if (repository.existsByUsername(username)) {
            return false
        }
        repository.save(UserCredential(username = username, password = password))
        return true
    }

    @org.springframework.context.annotation.Bean
    fun seedDefaultUsers() = ApplicationRunner {
        val defaults = listOf(
            "admin" to "admin123",
            "demo" to "demo123"
        )

        defaults.forEach { (username, password) ->
            if (!repository.existsByUsername(username)) {
                repository.save(UserCredential(username = username, password = password))
            }
        }
    }
}