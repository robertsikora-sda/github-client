package com.example.githubclient

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class GitHubClientApplication

fun main(args: Array<String>) {
    runApplication<GitHubClientApplication>(args = args)
}
