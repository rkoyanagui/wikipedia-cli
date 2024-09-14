package com.rkoyanagui.wikipediacli.service

import com.rkoyanagui.wikipediacli.client.WikipediaClient
import com.rkoyanagui.wikipediacli.config.BasicConfig
import com.rkoyanagui.wikipediacli.domain.Node
import com.rkoyanagui.wikipediacli.domain.Page
import com.rkoyanagui.wikipediacli.domain.Titles
import com.rkoyanagui.wikipediacli.dto.PageDto
import com.rkoyanagui.wikipediacli.dto.RelatedPagesDto
import com.rkoyanagui.wikipediacli.dto.TitlesDto
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import io.mockk.*
import org.slf4j.Logger
import reactor.core.publisher.Mono
import reactor.core.scheduler.Scheduler
import reactor.test.StepVerifier

@MicronautTest(application = BasicConfig::class)
class PageTreeServiceTest(private val scheduler: Scheduler) : FunSpec({

    lateinit var wikipediaClient: WikipediaClient
    lateinit var logger: Logger
    lateinit var pageTreeService: PageTreeService

    beforeTest {
        wikipediaClient = mockk()
        logger = mockk()
        pageTreeService = PageTreeService(wikipediaClient, scheduler, logger)
    }

    test("should find related pages and form a tree") {
        every { wikipediaClient.findPageSummary("My_Title") } returns
                Mono.just(HttpResponse.ok(PageDto(TitlesDto("My_Title", "My Title"))))
        every { wikipediaClient.findRelatedPages("My_Title") } returns
                Mono.just(
                    HttpResponse.ok(
                        RelatedPagesDto(
                            arrayOf(
                                PageDto(TitlesDto("Another_TitleA", "Another TitleA")),
                                PageDto(TitlesDto("Another_TitleB", "Another TitleB"))
                            )
                        )
                    )
                )
        every { wikipediaClient.findRelatedPages("Another_TitleA") } returns
                Mono.just(
                    HttpResponse.ok(
                        RelatedPagesDto(
                            arrayOf(
                                PageDto(TitlesDto("Some_Other_TitleA1", "Some Other TitleA1")),
                                PageDto(TitlesDto("Some_Other_TitleA2", "Some Other TitleA2"))
                            )
                        )
                    )
                )
        every { wikipediaClient.findRelatedPages("Another_TitleB") } returns
                Mono.just(
                    HttpResponse.ok(
                        RelatedPagesDto(
                            arrayOf(
                                PageDto(TitlesDto("Some_Other_TitleB1", "Some Other TitleB1")),
                                PageDto(TitlesDto("Some_Other_TitleB2", "Some Other TitleB2"))
                            )
                        )
                    )
                )
        val expected = Node(
            value = Page(Titles("My_Title", "My Title")),
            children = mutableMapOf(
                "Another_TitleA" to Node(
                    value = Page(Titles("Another_TitleA", "Another TitleA")),
                    children = mutableMapOf(
                        "Some_Other_TitleA1" to Node(Page(Titles("Some_Other_TitleA1", "Some Other TitleA1"))),
                        "Some_Other_TitleA2" to Node(Page(Titles("Some_Other_TitleA2", "Some Other TitleA2")))
                    )
                ),
                "Another_TitleB" to Node(
                    value = Page(Titles("Another_TitleB", "Another TitleB")),
                    children = mutableMapOf(
                        "Some_Other_TitleB1" to Node(Page(Titles("Some_Other_TitleB1", "Some Other TitleB1"))),
                        "Some_Other_TitleB2" to Node(Page(Titles("Some_Other_TitleB2", "Some Other TitleB2")))
                    )
                )
            )
        )
        StepVerifier.create(pageTreeService.findPageTree("My_Title", width = 2, depth = 2))
            .assertNext { actual -> actual shouldBe expected }
            .verifyComplete()
    }

    test("should apply depth limit") {
        every { wikipediaClient.findPageSummary("My_Title") } returns
                Mono.just(HttpResponse.ok(PageDto(TitlesDto("My_Title", "My Title"))))
        every { wikipediaClient.findRelatedPages("My_Title") } returns
                Mono.just(
                    HttpResponse.ok(
                        RelatedPagesDto(
                            arrayOf(
                                PageDto(TitlesDto("Another_Title", "Another Title")),
                                PageDto(TitlesDto("Another_Title2", "Another Title2"))
                            )
                        )
                    )
                )
        val expected = Node(
            value = Page(Titles("My_Title", "My Title")),
            children = mutableMapOf(
                "Another_Title" to Node(Page(Titles("Another_Title", "Another Title"))),
                "Another_Title2" to Node(Page(Titles("Another_Title2", "Another Title2")))
            )
        )
        StepVerifier.create(pageTreeService.findPageTree("My_Title", width = 2, depth = 1))
            .expectNext(expected)
            .verifyComplete()
    }

    test("should handle excess depth gracefully and not dig too greedily") {
        every { wikipediaClient.findPageSummary("My_Title") } returns
                Mono.just(HttpResponse.ok(PageDto(TitlesDto("My_Title", "My Title"))))
        every { wikipediaClient.findRelatedPages("My_Title") } returns
                Mono.just(
                    HttpResponse.ok(
                        RelatedPagesDto(
                            arrayOf(
                                PageDto(TitlesDto("Another_Title", "Another Title")),
                                PageDto(TitlesDto("Some_Other_Title", "Some Other Title"))
                            )
                        )
                    )
                )
        every { wikipediaClient.findRelatedPages("Another_Title") } returns Mono.empty()
        every { wikipediaClient.findRelatedPages("Some_Other_Title") } returns Mono.empty()
        val expected = Node(
            value = Page(Titles("My_Title", "My Title")),
            children = mutableMapOf(
                "Another_Title" to Node(Page(Titles("Another_Title", "Another Title"))),
                "Some_Other_Title" to Node(Page(Titles("Some_Other_Title", "Some Other Title")))
            )
        )
        StepVerifier.create(pageTreeService.findPageTree("My_Title", width = 2, depth = 3))
            .expectNext(expected)
            .verifyComplete()
    }

    test("should apply width limit") {
        every { wikipediaClient.findPageSummary("My_Title") } returns
                Mono.just(HttpResponse.ok(PageDto(TitlesDto("My_Title", "My Title"))))
        every { wikipediaClient.findRelatedPages("My_Title") } returns
                Mono.just(
                    HttpResponse.ok(
                        RelatedPagesDto(
                            arrayOf(
                                PageDto(TitlesDto("Another_TitleA", "Another TitleA")),
                                PageDto(TitlesDto("Another_TitleB", "Another TitleB"))
                            )
                        )
                    )
                )
        every { wikipediaClient.findRelatedPages("Another_TitleA") } returns
                Mono.just(
                    HttpResponse.ok(
                        RelatedPagesDto(
                            arrayOf(
                                PageDto(TitlesDto("Some_Other_TitleA1", "Some Other TitleA1")),
                                PageDto(TitlesDto("Some_Other_TitleA2", "Some Other TitleA2"))
                            )
                        )
                    )
                )
        val expected = Node(
            value = Page(Titles("My_Title", "My Title")),
            children = mutableMapOf(
                "Another_TitleA" to Node(
                    value = Page(Titles("Another_TitleA", "Another TitleA")),
                    children = mutableMapOf(
                        "Some_Other_TitleA1" to Node(Page(Titles("Some_Other_TitleA1", "Some Other TitleA1")))
                    )
                )
            )
        )
        StepVerifier.create(pageTreeService.findPageTree("My_Title", width = 1, depth = 2))
            .expectNext(expected)
            .verifyComplete()
    }

    test("should handle excess width gracefully and not overreach") {
        every { wikipediaClient.findPageSummary("My_Title") } returns
                Mono.just(HttpResponse.ok(PageDto(TitlesDto("My_Title", "My Title"))))
        every { wikipediaClient.findRelatedPages("My_Title") } returns
                Mono.just(
                    HttpResponse.ok(
                        RelatedPagesDto(
                            arrayOf(
                                PageDto(TitlesDto("Another_Title", "Another Title"))
                            )
                        )
                    )
                )
        every { wikipediaClient.findRelatedPages("Another_Title") } returns
                Mono.just(
                    HttpResponse.ok(
                        RelatedPagesDto(
                            arrayOf(
                                PageDto(TitlesDto("Some_Other_Title", "Some Other Title"))
                            )
                        )
                    )
                )
        val expected = Node(
            value = Page(Titles("My_Title", "My Title")),
            children = mutableMapOf(
                "Another_Title" to Node(
                    value = Page(Titles("Another_Title", "Another Title")),
                    children = mutableMapOf(
                        "Some_Other_Title" to Node(Page(Titles("Some_Other_Title", "Some Other Title")))
                    )
                )
            )
        )
        StepVerifier.create(pageTreeService.findPageTree("My_Title", width = 3, depth = 2))
            .expectNext(expected)
            .verifyComplete()
    }

    test("should log page not found and complete with empty result") {
        every { wikipediaClient.findPageSummary("My_Title") } returns Mono.empty()
        every { logger.info(any()) } just Runs
        StepVerifier.create(pageTreeService.findPageTree("My_Title", width = 1, depth = 1))
            .verifyComplete()
        verify { logger.info("Page 'My_Title' not found.") }
    }

    test("given a misspelled title then should redirect to the correct title and form a tree") {
        every { wikipediaClient.findPageSummary("My_Titel") } returns
                Mono.just(
                    HttpResponse.status<PageDto?>(HttpStatus.FOUND)
                        .header("Location", "My_Title")
                )
        every { wikipediaClient.findPageSummary("My_Title") } returns
                Mono.just(HttpResponse.ok(PageDto(TitlesDto("My_Title", "My Title"))))
        every { wikipediaClient.findRelatedPages("My_Title") } returns
                Mono.just(
                    HttpResponse.ok(
                        RelatedPagesDto(
                            arrayOf(
                                PageDto(TitlesDto("Another_Title", "Another Title"))
                            )
                        )
                    )
                )
        every { logger.warn(any()) } just Runs
        val expected = Node(
            value = Page(Titles("My_Title", "My Title")),
            children = mutableMapOf(
                "Another_Title" to Node(Page(Titles("Another_Title", "Another Title")))
            )
        )
        StepVerifier.create(pageTreeService.findPageTree("My_Titel", width = 1, depth = 1))
            .assertNext { actual -> actual shouldBe expected }
            .verifyComplete()
        verify { logger.warn("You searched for 'My_Titel'. Did you mean 'My_Title'?") }
    }
})
