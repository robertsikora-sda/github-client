package com.example.githubclient.userrepositories

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import spock.lang.Specification

class UserRepositoriesFetcherSpec extends Specification {

    def githubAPIClientMock = Mock(GithubAPIClient)
    def underTest = new UserRepositoriesFetcher(githubAPIClientMock)

    def "should fetch user repositories details skipping forks"() {
        given:
        githubAPIClientMock.fetchUserRepositories("test")
                >> Flux.just(
                new GithubUserRepository("repo-1", new GithubUserRepository.Owner("test"), false),
                new GithubUserRepository("repo-2", new GithubUserRepository.Owner("test"), false),
                new GithubUserRepository("repo-1-fork", new GithubUserRepository.Owner("test"), true)
        )

        githubAPIClientMock.fetchBranches("test", "repo-1") >> Mono.just(
                [new GithubBranch("main", new GithubBranch.Commit("sha-1")),
                 new GithubBranch("develop", new GithubBranch.Commit("sha-1"))] as Set

        )

        githubAPIClientMock.fetchBranches("test", "repo-2") >> Mono.just(
                [new GithubBranch("main", new GithubBranch.Commit("sha-2")),
                 new GithubBranch("develop", new GithubBranch.Commit("sha-2"))] as Set
        )

        githubAPIClientMock.fetchBranches("test", "repo-3") >> Mono.just(
                [new GithubBranch("main", new GithubBranch.Commit("sha-3")),
                 new GithubBranch("develop", new GithubBranch.Commit("sha-3"))] as Set
        )

        expect:
        StepVerifier
                .create(underTest.fetchRepositories("test"))
                .expectNext(new UserRepositoryDetails("repo-1", "test",
                        [new UserRepositoryDetails.BranchDetails("main", "sha-1"),
                         new UserRepositoryDetails.BranchDetails("develop", "sha-1")]))
                .expectNext(new UserRepositoryDetails("repo-2", "test",
                        [new UserRepositoryDetails.BranchDetails("main", "sha-2"),
                         new UserRepositoryDetails.BranchDetails("develop", "sha-2")]))
                .expectComplete()
                .verify()
    }
}
