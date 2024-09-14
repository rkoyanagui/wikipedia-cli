package com.rkoyanagui.wikipediacli.service

import com.rkoyanagui.wikipediacli.domain.Node
import com.rkoyanagui.wikipediacli.domain.Page
import com.rkoyanagui.wikipediacli.domain.Titles
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.slf4j.Logger
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class ExpandTreeServiceTest : FunSpec({

    lateinit var pageTreeService: PageTreeService
    lateinit var treePrinterService: TreePrinterService
    lateinit var logger: Logger
    lateinit var expandTreeService: ExpandTreeService

    beforeTest {
        pageTreeService = mockk()
        treePrinterService = mockk()
        logger = mockk()
        expandTreeService = ExpandTreeService(pageTreeService, treePrinterService, logger)
    }

    test("should reject empty title") {
        StepVerifier.create(expandTreeService.findPageTree(title = "", width = 1, depth = 1))
            .verifyErrorSatisfies { e ->
                e.javaClass shouldBe IllegalArgumentException::class.java
                e.message shouldBe "Title should not be empty"
            }
    }

    test("should reject width less than one") {
        StepVerifier.create(expandTreeService.findPageTree(title = "abc", width = 0, depth = 1))
            .verifyErrorSatisfies { e ->
                e.javaClass shouldBe IllegalArgumentException::class.java
                e.message shouldBe "Width should not be zero or less"
            }
    }

    test("should reject depth less than one") {
        StepVerifier.create(expandTreeService.findPageTree(title = "abc", width = 1, depth = 0))
            .verifyErrorSatisfies { e ->
                e.javaClass shouldBe IllegalArgumentException::class.java
                e.message shouldBe "Depth should not be zero or less"
            }
    }

    test("should expand a tree of related pages in printed form") {
        val tree = Node(
            value = Page(Titles("My_Title", "My Title")),
            children = mutableMapOf(
                "Another_Title" to Node(Page(Titles("Another_Title", "Another Title")))
            )
        )
        every { pageTreeService.findPageTree("My_Title", 1, 1) } returns Mono.just(tree)

        val printedTree = """
                My_Title
                |- Another_Title""".trimIndent()
        every { treePrinterService.formatTree(tree) } returns Mono.just(printedTree)

        StepVerifier.create(expandTreeService.findPageTree(" My Title ", 1, 1))
            .expectNext(printedTree)
            .verifyComplete()
    }
})
