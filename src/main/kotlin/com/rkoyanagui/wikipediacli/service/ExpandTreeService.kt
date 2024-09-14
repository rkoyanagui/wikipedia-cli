package com.rkoyanagui.wikipediacli.service

import io.micronaut.http.client.exceptions.HttpClientResponseException
import jakarta.inject.Singleton
import org.slf4j.Logger
import reactor.core.publisher.Mono

@Singleton
class ExpandTreeService(
    private val pageTreeService: PageTreeService,
    private val treePrinterService: TreePrinterService,
    private val log: Logger,
) {
    fun findPageTree(title: String, width: Int, depth: Int): Mono<String> {
        return Mono.justOrEmpty(title)
            .filter { s -> s.isNotBlank() }
            .switchIfEmpty(Mono.error { IllegalArgumentException("Title should not be empty") })
            .flatMap {
                Mono.justOrEmpty(width)
                    .filter { w -> w > 0 }
                    .switchIfEmpty(Mono.error { IllegalArgumentException("Width should not be zero or less") })
            }.flatMap {
                Mono.justOrEmpty(depth)
                    .filter { d -> d > 0 }
                    .switchIfEmpty(Mono.error { IllegalArgumentException("Depth should not be zero or less") })
            }.flatMap {
                val canonicalTitle = toCanonical(title)
                pageTreeService.findPageTree(canonicalTitle, width, depth)
                    .doOnError(HttpClientResponseException::class.java)
                    { e -> log.error("Web client exception with status code ${e.status} and response body ${e.response}") }
                    .doOnError { e -> log.error(e.message, e) }
                    .flatMap { pageTree -> treePrinterService.formatTree(pageTree) }
                    .onErrorComplete()
            }
    }

    private fun toCanonical(title: String): String {
        return title.trim().replace(" ", "_")
    }
}
