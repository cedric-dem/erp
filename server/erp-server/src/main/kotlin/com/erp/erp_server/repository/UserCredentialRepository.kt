package com.erp.erp_server

import org.springframework.data.jpa.repository.JpaRepository

interface UserCredentialRepository : JpaRepository<UserCredential, Long> {
    fun existsByUsername(username: String): Boolean
    fun existsByUsernameAndPassword(username: String, password: String): Boolean
}