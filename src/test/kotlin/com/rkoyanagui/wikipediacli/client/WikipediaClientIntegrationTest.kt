package com.rkoyanagui.wikipediacli.client

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo
import com.rkoyanagui.wikipediacli.dto.PageDto
import com.rkoyanagui.wikipediacli.dto.RelatedPagesDto
import com.rkoyanagui.wikipediacli.dto.TitlesDto
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import reactor.test.StepVerifier

@MicronautTest
class WikipediaClientIntegrationTest(
    private val wikipediaClient: WikipediaClient,
) : FunSpec({

    lateinit var wireMockServer: WireMockServer
    lateinit var wireMock: WireMock
    lateinit var wmri: WireMockRuntimeInfo

    beforeSpec {
        val config = WireMockConfiguration()
        config.port(9111)
        wireMockServer = WireMockServer(config)
        wireMockServer.start()
        wmri = WireMockRuntimeInfo(wireMockServer)
        wireMock = wmri.wireMock
    }

    beforeTest {
        wireMock.resetRequests()
        wireMock.resetMappings()
        wireMock.resetScenarios()
    }

    afterSpec {
        wireMockServer.stop()
    }

    test("find page summary") {
        wireMock.register(
            get(urlPathEqualTo("/page/summary/Winston_Churchill"))
                .withHeader("User-Agent", equalTo("coolbot/1.0.0 (https://coolbot.com; coolbot@example.org)"))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json; charset=utf-8")
                        .withStatus(200)
                        .withBodyFile("page/summary/200-ok.json")
                )
        )
        val expected = PageDto(
            titles = TitlesDto(
                "Winston_Churchill",
                "Winston Churchill"
            ), description = "British statesman and writer (1874–1965)"
        )
        StepVerifier.create(wikipediaClient.findPageSummary("Winston_Churchill"))
            .expectNextMatches { resp -> resp.body().equals(expected) }
            .verifyComplete()
    }

    test("given non-existent title then should not find page summary") {
        wireMock.register(
            get(urlPathEqualTo("/page/summary/erp_derp"))
                .withHeader("User-Agent", equalTo("coolbot/1.0.0 (https://coolbot.com; coolbot@example.org)"))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json; charset=utf-8")
                        .withStatus(404)
                        .withBodyFile("page/summary/404-not-found.json")
                )
        )
        StepVerifier.create(wikipediaClient.findPageSummary("erp_derp"))
            .verifyComplete()
    }

    test("given a misspelled title then should redirect to the correct title") {
        wireMock.register(
            get(urlPathEqualTo("/page/summary/Winston_Churchil"))
                .withHeader("User-Agent", equalTo("coolbot/1.0.0 (https://coolbot.com; coolbot@example.org)"))
                .willReturn(
                    aResponse()
                        .withHeader("Location", "Winston_Churchill")
                        .withStatus(302)
                )
        )
        StepVerifier.create(wikipediaClient.findPageSummary("Winston_Churchil"))
            .assertNext { resp -> resp.header("Location") shouldBe "Winston_Churchill" }
            .verifyComplete()
    }

    test("find related pages") {
        wireMock.register(
            get(urlPathEqualTo("/page/related/Winston_Churchill"))
                .withHeader("User-Agent", equalTo("coolbot/1.0.0 (https://coolbot.com; coolbot@example.org)"))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json; charset=utf-8")
                        .withStatus(200)
                        .withBodyFile("page/related/200-ok.json")
                )
        )
        val expected = RelatedPagesDto(
            arrayOf(
                PageDto(
                    titles = TitlesDto(
                        "H._H._Asquith",
                        "H. H. Asquith"
                    ), description = "Prime Minister of the United Kingdom from 1908 to 1916"
                ),
                PageDto(
                    titles = TitlesDto(
                        "1945_United_Kingdom_general_election",
                        "1945 United Kingdom general election"
                    )
                )
            )
        )
        StepVerifier.create(wikipediaClient.findRelatedPages("Winston_Churchill"))
            .expectNextMatches { resp -> resp.body().equals(expected) }
            .verifyComplete()
    }

    test("given a non-existent title then should not find related pages") {
        wireMock.register(
            get(urlPathEqualTo("/page/related/erp_derp"))
                .withHeader("User-Agent", equalTo("coolbot/1.0.0 (https://coolbot.com; coolbot@example.org)"))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json; charset=utf-8")
                        .withStatus(404)
                        .withBodyFile("page/related/404-not-found.json")
                )
        )
        StepVerifier.create(wikipediaClient.findRelatedPages("erp_derp"))
            .verifyComplete()
    }
})
