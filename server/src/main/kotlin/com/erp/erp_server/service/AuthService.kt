package com.erp.erp_server

import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val repository: UserCredentialRepository
) {
    enum class RegisterResult {
        SUCCESS,
        USERNAME_EXISTS,
        PROJECT_EXISTS,
        PROJECT_NOT_FOUND,
        INVALID_PROJECT_ACTION,
        SAVE_FAILED
    }

    fun isValidLogin(username: String, password: String): Boolean {
        return repository.existsByUsernameAndPassword(username, password)
    }

    fun register(username: String, password: String, project: String, projectAction: String): RegisterResult {
        if (repository.existsByUsername(username)) {
            return RegisterResult.USERNAME_EXISTS
        }

        if (projectAction != "join" && projectAction != "create") {
            return RegisterResult.INVALID_PROJECT_ACTION
        }

        if (projectAction == "join" && !repository.existsByProject(project)) {
            return RegisterResult.PROJECT_NOT_FOUND
        }

        if (projectAction == "create" && repository.existsByProject(project)) {
            return RegisterResult.PROJECT_EXISTS
        }

        val userType = if (projectAction == "create") "ADMIN" else "NEW_USER"
        return try {
            repository.save(
                UserCredential(
                    username = username,
                    password = password,
                    project = project,
                    userType = userType
                )
            )
            RegisterResult.SUCCESS
        } catch (_: DataIntegrityViolationException) {
            RegisterResult.SAVE_FAILED
        }
    }

    fun findProjectByUsername(username: String): String? {
        return repository.findByUsernameIgnoreCase(username)?.project
    }
}