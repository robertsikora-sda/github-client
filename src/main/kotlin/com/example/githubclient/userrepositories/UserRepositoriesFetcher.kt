package com.example.githubclient.userrepositories

import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Service
class UserRepositoriesFetcher(private val githubAPIClient: GithubAPIClient) {

    fun fetchRepositories(username: String): Flux<UserRepositoryDetails> =
        githubAPIClient.fetchUserRepositories(username)
            .filter { !it.fork }
            .flatMap { repo ->
                githubAPIClient.fetchBranches(username, repo.name)
                    .map { branch ->
                        UserRepositoryDetails(
                            repo.name,
                            repo.owner.login,
                            branch.map { it.toBranchDetails() }
                        )
                    }
            }


    private fun GithubBranch.toBranchDetails(): UserRepositoryDetails.BranchDetails =
        UserRepositoryDetails.BranchDetails(
            branchName = this.name,
            lastCommitSha = this.commit.sha
        )
}
