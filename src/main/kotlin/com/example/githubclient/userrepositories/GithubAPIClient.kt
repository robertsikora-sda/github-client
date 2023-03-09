package com.example.githubclient.userrepositories

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders.ACCEPT
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToFlux
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

data class GithubUserRepository(
    val name: String,
    val owner: Owner,
    val fork: Boolean
) {
    data class Owner(val login: String)
}

data class GithubBranch(val name: String, val commit: Commit) {
    data class Commit(val sha: String?)
}

@Component
class GithubAPIClient(private val webClient: WebClient) {

    fun fetchUserRepositories(username: String): Flux<GithubUserRepository> =
        webClient.get()
            .uri("/users/$username/repos", username)
            .exchangeToFlux {
                if (it.statusCode() == HttpStatus.NOT_FOUND) {
                    Flux.empty()
                } else {
                    it.bodyToFlux()
                }
            }

    fun fetchBranches(username: String, repositoryName: String): Mono<Set<GithubBranch>> = webClient.get()
        .uri("/repos/$username/$repositoryName/branches", username, repositoryName)
        .retrieve()
        .bodyToMono()

}

@Configuration
@EnableConfigurationProperties(WebClientConfiguration.WebClientProperties::class)
class WebClientConfiguration {

    @ConfigurationProperties(prefix = "application.github-api-client")
    data class WebClientProperties(val baseUrl: String, val apiVersion: String, val token: String)

    @Bean
    fun webClient(properties: WebClientProperties): WebClient =
        WebClient
            .builder()
            .baseUrl(properties.baseUrl)
            .defaultHeaders {
                it.set(ACCEPT, GITHUB_ACCEPTED_MEDIA_TYPE)
                it.set(GITHUB_API_VERSION_HEADER, properties.apiVersion)
                it.setBearerAuth(properties.token)
            }
            .build()

    companion object {
        private const val GITHUB_ACCEPTED_MEDIA_TYPE = "application/vnd.github+json"
        private const val GITHUB_API_VERSION_HEADER = "X-GitHub-Api-Version"
    }

}
