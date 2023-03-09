package com.example.githubclient

import com.example.githubclient.userrepositories.ErrorResponse
import com.example.githubclient.userrepositories.GithubBranch
import com.example.githubclient.userrepositories.GithubUserRepository
import com.example.githubclient.userrepositories.UserRepositoryDetails
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.reactive.server.WebTestClient
import spock.lang.Specification

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse
import static com.github.tomakehurst.wiremock.client.WireMock.get
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo

@SpringBootTest(classes = [GitHubClientApplication.class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration
class GitHubClientIT extends Specification {

    private WIREMOCK_PORT = 18888

    @Autowired
    WebTestClient webTestClient

    WireMockServer wireMockServer

    void setup() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().port(WIREMOCK_PORT))
        wireMockServer.start()
    }

    void cleanup() {
        wireMockServer.stop()
    }

    def "should return all repositories belong to given user"() {
        given:
        stubServerResponseForSuccess()

        expect:
        webTestClient
                .get()
                .uri("/api/v1/users/test/repositories")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(List<UserRepositoryDetails>.class).isEqualTo(
                [
                        [
                                repositoryName: "repo-1", ownerLogin: "test", branches: [
                                [branchName: "develop", lastCommitSha: "sha-1"],
                                [branchName: "main", lastCommitSha: "sha-2"]
                        ].sort()
                        ]

                ]
        )
    }

    def "should return error response on user not found"() {
        given:
        stubServerResponseForUserNotFound()

        expect:
        webTestClient
                .get()
                .uri("/api/v1/users/test/repositories")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(ErrorResponse.class).isEqualTo(
                new ErrorResponse(404, "Repository for 'test' not found")
        )
    }

    def "should return error response on application/xml content type"() {
        expect:
        webTestClient
                .get()
                .uri("/api/v1/users/test/repositories")
                .accept(MediaType.APPLICATION_XML)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.NOT_ACCEPTABLE)
                .expectBody(ErrorResponse.class).isEqualTo(
                new ErrorResponse(406, "Media type 'application/xml' is not supported")
        )
    }

    private StubMapping stubServerResponseForSuccess() {
        wireMockServer.stubFor(get(urlPathEqualTo("/users/test/repos"))
                .willReturn(aResponse().okForJson(
                        [new GithubUserRepository("repo-1", new GithubUserRepository.Owner("test"), false)]
                )))

        wireMockServer.stubFor(get(urlPathEqualTo("/repos/test/repo-1/branches"))
                .willReturn(aResponse().okForJson(
                        [
                                new GithubBranch("develop", new GithubBranch.Commit("sha-1")),
                                new GithubBranch("main", new GithubBranch.Commit("sha-2"))
                        ].sort()
                )))
    }

    private StubMapping stubServerResponseForUserNotFound() {
        wireMockServer.stubFor(get(urlPathEqualTo("/users/test/repos"))
                .willReturn(aResponse().withStatus(404)))
    }
}
