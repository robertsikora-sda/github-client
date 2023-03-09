package com.example.githubclient.userrepositories

import org.springframework.http.HttpStatus

data class UserRepositoryDetails(
    val repositoryName: String,
    val ownerLogin: String,
    val branches: List<BranchDetails>
) {
    data class BranchDetails(val branchName: String, val lastCommitSha: String?)
}

data class ErrorResponse(val status: Int, val message: String)

fun notFoundErrorResponse(username: String) =
    ErrorResponse(HttpStatus.NOT_FOUND.value(), "Repository for '$username' not found")

fun notAcceptableErrorResponse() =
    ErrorResponse(HttpStatus.NOT_ACCEPTABLE.value(), "Media type 'application/xml' is not supported")
