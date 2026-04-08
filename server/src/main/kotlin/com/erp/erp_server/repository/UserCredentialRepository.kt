package com.erp.erp_server

import org.springframework.data.jpa.repository.JpaRepository

interface UserCredentialRepository : JpaRepository<UserCredential, Long> {
    fun existsByUsername(username: String): Boolean
    fun existsByProject(project: String): Boolean
    fun existsByUsernameAndPassword(username: String, password: String): Boolean
    fun findByUsername(username: String): UserCredential?
    fun findByUsernameIgnoreCase(username: String): UserCredential?
    fun findAllByProject(project: String): List<UserCredential>
}