package com.rkoyanagui.wikipediacli.client

import com.rkoyanagui.wikipediacli.dto.PageDto
import com.rkoyanagui.wikipediacli.dto.RelatedPagesDto
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Header
import io.micronaut.http.client.annotation.Client
import reactor.core.publisher.Mono

@Client("\${wikipedia.base-url}")
@Header(
    name = "Accept",
    value = "application/json; charset=utf-8; profile=\"https://www.mediawiki.org/wiki/Specs/Summary/1.4.2\""
)
@Header(name = "User-Agent", value = "\${wikipedia.headers.user-agent}")
interface WikipediaClient {

    @Get("/page/summary/{title}")
    fun findPageSummary(title: String): Mono<HttpResponse<PageDto>>

    @Get("/page/related/{title}")
    fun findRelatedPages(title: String): Mono<HttpResponse<RelatedPagesDto>>
}
