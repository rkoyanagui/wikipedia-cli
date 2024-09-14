package com.rkoyanagui.wikipediacli.command

import com.rkoyanagui.wikipediacli.service.ExpandTreeService
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldContain
import io.micronaut.configuration.picocli.PicocliRunner
import io.micronaut.context.ApplicationContext
import io.micronaut.context.env.Environment
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import io.mockk.every
import io.mockk.mockk
import reactor.core.publisher.Mono
import java.io.ByteArrayOutputStream
import java.io.PrintStream

@MicronautTest(environments = [Environment.TEST, Environment.CLI])
class ExpandTreeCommandTest(
    private val ctx: ApplicationContext,
) : FunSpec({

    lateinit var expandTreeService: ExpandTreeService

    beforeTest {
        expandTreeService = mockk()
        ctx.registerSingleton(ExpandTreeService::class.java, expandTreeService)
    }

    test("show help message") {
        val expectedConsoleOutput =
            "Finds pages related to the given title, then find pages related to those, and so on, forming a tree of pages."

        val baos = ByteArrayOutputStream()
        val out = System.out
        System.setOut(PrintStream(baos))

        val args = "-h".split(" ").toTypedArray()
        PicocliRunner.run(ExpandTreeCommand::class.java, ctx, *args)
        out.println(baos)
        val outputText = baos.toString().trim().replace(Regex("[\\h\\s]+"), " ")

        outputText shouldContain expectedConsoleOutput
    }

    test("expand wikipedia article tree") {
        val tree = "My_Title\n  |-Another_Title"
        every { expandTreeService.findPageTree("My_Title", 3, 3) } returns Mono.just(tree)

        val baos = ByteArrayOutputStream()
        val out = System.out
        System.setOut(PrintStream(baos))

        val args = "-r My_Title".split(" ").toTypedArray()
        PicocliRunner.run(ExpandTreeCommand::class.java, ctx, *args)
        out.println(baos)
        val outputText = baos.toString()

        outputText shouldContain tree
    }
})
