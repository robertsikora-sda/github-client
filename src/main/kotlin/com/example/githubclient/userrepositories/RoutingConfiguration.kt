package com.example.githubclient.userrepositories

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.APPLICATION_XML
import org.springframework.web.reactive.function.server.RequestPredicates.GET
import org.springframework.web.reactive.function.server.RequestPredicates.accept
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions.route
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.ServerResponse.status
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Configuration
class RoutingConfiguration(private val userRepositoriesFetcher: UserRepositoriesFetcher) {

    @Bean
    fun routes(): RouterFunction<ServerResponse> {
        return route(
            GET("/api/v1/users/{username}/repositories").and(accept(APPLICATION_JSON))
        ) { request ->
            val username = request.pathVariable("username")
            userRepositoriesFetcher.fetchRepositories(username)
                .collectListOrEmpty()
                .flatMap { okResponseWith(it) }
                .switchIfEmpty(notFoundResponseWith(notFoundErrorResponse(username)))
        }.and(
            route(
                GET("/api/v1/users/{username}/repositories").and(accept(APPLICATION_XML))
            ) { notAcceptableResponseWith(notAcceptableErrorResponse()) }
        )
    }
}

private fun okResponseWith(body: Any): Mono<ServerResponse> {
    return ok().bodyValue(body)
}

private fun notFoundResponseWith(errorResponse: ErrorResponse): Mono<ServerResponse> {
    return status(HttpStatus.NOT_FOUND).bodyValue(errorResponse)
}

private fun notAcceptableResponseWith(errorResponse: ErrorResponse): Mono<ServerResponse> {
    return status(HttpStatus.NOT_ACCEPTABLE).bodyValue(errorResponse)
}

private fun <R> Flux<R>.collectListOrEmpty(): Mono<List<R>> = this.collectList().flatMap {
    val result = if (it.isEmpty()) {
        Mono.empty()
    } else {
        Mono.just(it)
    }
    result
}

