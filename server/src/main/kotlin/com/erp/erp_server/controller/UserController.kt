package com.erp.erp_server

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import org.springframework.http.HttpStatus

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

    @PatchMapping("/api/users/{id}/make-normal")
    fun makeUserNormal(
        @PathVariable id: Long,
        @RequestParam username: String
    ): UserSummaryResponse {
        val requester = userCredentialRepository.findByUsername(username.trim())
            ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "Unknown user")

        val target = userCredentialRepository.findById(id).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
        }

        val requesterProject = projectAccessService.resolveProjectOrThrow(requester.username)
        val requesterIsNewUser = requester.userType.trim().equals("NEW_USER", ignoreCase = true)
        val requesterCanApproveNewUsers = !requesterIsNewUser
        val requesterIsTargetUser = requester.id == target.id

        if (!requesterCanApproveNewUsers && !requesterIsTargetUser) {
            throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Only non-new users can change other users"
            )
        }

        if (!target.project.equals(requesterProject, ignoreCase = true)) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot modify users from another project")
        }

        if (!target.userType.equals("NEW_USER", ignoreCase = true)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Only new_user can be switched to normal")
        }

        val updatedUser = userCredentialRepository.save(
            UserCredential(
                id = target.id,
                username = target.username,
                password = target.password,
                project = target.project,
                userType = "NORMAL"
            )
        )

        return UserSummaryResponse(
            id = updatedUser.id,
            username = updatedUser.username,
            userType = updatedUser.userType.lowercase()
        )
    }
}
