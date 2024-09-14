package com.rkoyanagui.wikipediacli.command

import com.rkoyanagui.wikipediacli.service.ExpandTreeService
import jakarta.inject.Inject
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import java.time.Duration

@Command(
    name = "expand-tree",
    description = ["Finds pages related to the given title, then find pages related to those, and so on, forming a tree of pages."],
    mixinStandardHelpOptions = true
)
class ExpandTreeCommand : Runnable {

    @Option(
        names = ["-h", "--help"],
        usageHelp = true,
        description = ["Shows this help message and quits."]
    )
    var usageHelpRequested = false

    @Option(
        names = ["-r", "--root-title"],
        description = ["Title of root article."],
        required = true
    )
    lateinit var title: String

    @Option(
        names = ["-w", "--width"],
        description = ["Max search width (default: 3)."]
    )
    var width: Int = 3

    @Option(
        names = ["-d", "--depth"],
        description = ["Max search depth (default: 3)."]
    )
    var depth: Int = 3

    @Option(
        names = ["-t", "--timeout"],
        description = ["Max time to search in seconds (default: 60)."]
    )
    var timeout: Long = 60L

    @Inject
    lateinit var expandTreeService: ExpandTreeService

    override fun run() {
        expandTreeService.findPageTree(title, width, depth)
            .doOnNext { s -> println(s) }
            .block(Duration.ofSeconds(timeout))
    }
}
