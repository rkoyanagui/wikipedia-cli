package com.rkoyanagui.wikipediacli

import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.string.shouldContain
import io.micronaut.configuration.picocli.PicocliRunner
import io.micronaut.context.ApplicationContext
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import java.io.ByteArrayOutputStream
import java.io.PrintStream

@MicronautTest
class WikipediaCliCommandTest(
    private val ctx: ApplicationContext
) : FunSpec({
    withData(
        Pair("--help", "Shows this help message and quits"),
        Pair("--version", "1.0.0    Launch version")
    ) { pair ->
        val arg = pair.first
        val expectedConsoleOutput = pair.second

        val baos = ByteArrayOutputStream()
        val out = System.out
        System.setOut(PrintStream(baos))

        val args = arg.split(" ").toTypedArray()
        PicocliRunner.run(WikipediaCliCommand::class.java, ctx, *args)
        out.println(baos)
        val outputText = baos.toString()

        outputText shouldContain expectedConsoleOutput
    }
})
