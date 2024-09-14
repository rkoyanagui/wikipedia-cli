package com.rkoyanagui.wikipediacli.service

import com.rkoyanagui.wikipediacli.domain.Node
import com.rkoyanagui.wikipediacli.domain.Page
import com.rkoyanagui.wikipediacli.domain.Titles
import io.kotest.core.spec.style.BehaviorSpec
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import reactor.test.StepVerifier

@MicronautTest
class TreePrinterServiceTest(
    private val treePrinterService: TreePrinterService
) : BehaviorSpec({

    given("a single tree node") {
        val tree = Node(
            value = Page(Titles("Title_0", "Title 0")),
            children = mutableMapOf()
        )

        then("it should properly format it") {
            val expected = "Title_0"

            StepVerifier.create(treePrinterService.formatTree(tree))
                .expectNext(expected)
                .verifyComplete()
        }
    }

    given("a tree 3 deep 1 wide") {
        val tree = Node(
            value = Page(Titles("Title_0", "Title 0")),
            children = mutableMapOf(
                "Title_1" to Node(
                    value = Page(Titles("Title_1", "Title 1")),
                    children = mutableMapOf(
                        "Title_1.1" to Node(
                            value = Page(Titles("Title_1.1", "Title 1.1")),
                            children = mutableMapOf(
                                "Title_1.1.1" to Node(Page(Titles("Title_1.1.1", "Title 1.1.1")))
                            )
                        )
                    )
                )
            )
        )

        then("it should properly format it") {
            val expected = """
                Title_0
                |- Title_1
                   |- Title_1.1
                      |- Title_1.1.1""".trimIndent()

            StepVerifier.create(treePrinterService.formatTree(tree))
                .expectNext(expected)
                .verifyComplete()
        }
    }

    given("a tree 1 deep 3 wide") {
        val tree = Node(
            value = Page(Titles("Title_0", "Title 0")),
            children = mutableMapOf(
                "Title_1" to Node(Page(Titles("Title_1", "Title 1"))),
                "Title_2" to Node(Page(Titles("Title_2", "Title 2"))),
                "Title_3" to Node(Page(Titles("Title_3", "Title 3")))
            )
        )

        then("it should properly format it") {
            val expected = """
                Title_0
                |- Title_1
                |- Title_2
                |- Title_3""".trimIndent()

            StepVerifier.create(treePrinterService.formatTree(tree))
                .expectNext(expected)
                .verifyComplete()
        }
    }

    given("a tree 2 deep 2 wide") {
        val tree = Node(
            value = Page(Titles("Title_0", "Title 0")),
            children = mutableMapOf(
                "Title_1" to Node(
                    value = Page(Titles("Title_1", "Title 1")),
                    children = mutableMapOf(
                        "Title_1.1" to Node(Page(Titles("Title_1.1", "Title 1.1")))
                    )
                ),
                "Title_2" to Node(
                    value = Page(Titles("Title_2", "Title 2"))
                )
            )
        )

        then("it should properly format it") {
            val expected = """
                Title_0
                |- Title_1
                |  |- Title_1.1
                |- Title_2""".trimIndent()

            StepVerifier.create(treePrinterService.formatTree(tree))
                .expectNext(expected)
                .verifyComplete()
        }
    }

    given("a tree 5 deep 3 wide") {
        //@formatter:off
        val tree = Node(
            value = Page(Titles("Title_0", "Title 0")),
            children = mutableMapOf(
                "Title_1" to Node(
                    value = Page(Titles("Title_1", "Title 1")),
                    children = mutableMapOf(
                        "Title_1.1" to Node(
                            value = Page(Titles("Title_1.1", "Title 1.1")),
                            children = mutableMapOf(
                                "Title_1.1.1" to Node(Page(Titles("Title_1.1.1", "Title 1.1.1"))),
                                "Title_1.1.2" to Node(
                                    value = Page(Titles("Title_1.1.2", "Title 1.1.2")),
                                    children = mutableMapOf(
                                        "Title_1.1.2.1" to Node(
                                            value = Page(Titles("Title_1.1.2.1", "Title 1.1.2.1")),
                                            children = mutableMapOf(
                                                "Title_1.1.2.1.1" to Node(Page(Titles("Title_1.1.2.1.1", "Title 1.1.2.1.1")))
                                            )
                                        )
                                    )
                                ),
                                "Title_1.1.3" to Node(
                                    value = Page(Titles("Title_1.1.3", "Title 1.1.3")),
                                    children = mutableMapOf(
                                        "Title_1.1.3.1" to Node(
                                            value = Page(Titles("Title_1.1.3.1", "Title 1.1.3.1")),
                                            children = mutableMapOf(
                                                "Title_1.1.3.1.1" to Node(Page(Titles("Title_1.1.3.1.1", "Title 1.1.3.1.1")))
                                            )
                                        )
                                    )
                                )
                            )
                        ),
                        "Title_1.2" to Node(
                            value = Page(Titles("Title_1.2", "Title 1.2")),
                            children = mutableMapOf(
                                "Title_1.2.1" to Node(Page(Titles("Title_1.2.1", "Title 1.2.1"))),
                                "Title_1.2.2" to Node(Page(Titles("Title_1.2.2", "Title 1.2.2"))),
                                "Title_1.2.3" to Node(Page(Titles("Title_1.2.3", "Title 1.2.3")))
                            )
                        )
                    )
                ),
                "Title_2" to Node(
                    value = Page(Titles("Title_2", "Title 2"))
                )
            )
        )
        //@formatter:on

        then("it should properly format it") {
            val expected = """
                Title_0
                |- Title_1
                |  |- Title_1.1
                |  |  |- Title_1.1.1
                |  |  |- Title_1.1.2
                |  |  |  |- Title_1.1.2.1
                |  |  |     |- Title_1.1.2.1.1
                |  |  |- Title_1.1.3
                |  |     |- Title_1.1.3.1
                |  |        |- Title_1.1.3.1.1
                |  |- Title_1.2
                |     |- Title_1.2.1
                |     |- Title_1.2.2
                |     |- Title_1.2.3
                |- Title_2""".trimIndent()

            StepVerifier.create(treePrinterService.formatTree(tree))
                .expectNext(expected)
                .verifyComplete()
        }
    }
})
