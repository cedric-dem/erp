package com.erp.erp_server

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

data class UserSummaryResponse(
    val id: Long,
    val username: String,
    val userType: String
)

@RestController
class UserController(
    private val userCredentialRepository: UserCredentialRepository,
    private val projectAccessService: ProjectAccessService
) {
    @GetMapping("/api/users")
    fun users(@RequestParam username: String): List<UserSummaryResponse> {
        val project = projectAccessService.resolveProjectOrThrow(username)

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