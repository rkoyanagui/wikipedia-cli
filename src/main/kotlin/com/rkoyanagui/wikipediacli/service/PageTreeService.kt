package com.rkoyanagui.wikipediacli.service

import com.rkoyanagui.wikipediacli.client.WikipediaClient
import com.rkoyanagui.wikipediacli.domain.Node
import com.rkoyanagui.wikipediacli.domain.Page
import com.rkoyanagui.wikipediacli.domain.Titles
import com.rkoyanagui.wikipediacli.dto.PageDto
import io.micronaut.http.HttpStatus
import jakarta.inject.Singleton
import org.slf4j.Logger
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Scheduler
import java.util.stream.Collector

@Singleton
class PageTreeService(
    private val wikipediaClient: WikipediaClient,
    private val scheduler: Scheduler,
    private val log: Logger,
) {
    private val nodeCollector: Collector<Node<Page>, *, MutableMap<String, Node<Page>>> =
        Collector.of(
            { LinkedHashMap() },
            { map, node -> map[node.value.titles.canonical] = node },
            { map1, map2 ->
                map1.putAll(map2)
                map1
            }
        )

    fun findPageTree(title: String, width: Int, depth: Int): Mono<Node<Page>> {
        return wikipediaClient.findPageSummary(title)
            .switchIfEmpty(Mono.fromRunnable { log.info("Page '$title' not found.") })
            .flatMap { rsp ->
                Mono.just(rsp)
                    .filter { r -> HttpStatus.FOUND == r.status() }
                    .flatMap { r ->
                        val location = r.header("Location")
                        log.warn("You searched for '$title'. Did you mean '$location'?")
                        wikipediaClient.findPageSummary(location)
                    }.defaultIfEmpty(rsp)
            }
            .mapNotNull { r -> r.body() }
            .map { pageDto -> toPage(pageDto as PageDto) }
            .map { page -> Node(page) }
            .flatMap { ancestor -> expandFamilyTree(ancestor, width, 0, depth) }
    }

    private fun expandFamilyTree(
        ancestor: Node<Page>,
        width: Int,
        currentDepth: Int,
        maxDepth: Int
    ): Mono<Node<Page>> {
        return Mono.just(ancestor)
            .filter { _ -> currentDepth < maxDepth }
            .flatMap { parent -> addChildrenToParent(parent, width) }
            .flatMapMany { parent ->
                Flux.fromIterable(parent.children.values)
                    .parallel().runOn(scheduler)
                    .flatMap { child ->
                        expandFamilyTree(
                            child,
                            width,
                            currentDepth + 1,
                            maxDepth
                        )
                    }.sequential()
            }.then(Mono.just(ancestor))
    }

    private fun addChildrenToParent(parent: Node<Page>, width: Int): Mono<Node<Page>> {
        return wikipediaClient.findRelatedPages(parent.value.titles.canonical)
            .map { r -> r.body() }
            .flatMapIterable { response -> response.pages.asIterable() }
            .take(width.toLong())
            .map { pageDto -> toPage(pageDto) }
            .map { page -> Node(page) }
            .collect(nodeCollector)
            .map { children -> parent.addChildren(children) }
    }

    private fun toPage(pageDto: PageDto): Page {
        return Page(
            Titles(
                pageDto.titles.canonical,
                pageDto.titles.normalised
            ),
            pageDto.description
        )
    }
}
