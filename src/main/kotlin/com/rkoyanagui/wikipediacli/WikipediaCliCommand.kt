package com.rkoyanagui.wikipediacli

import com.rkoyanagui.wikipediacli.command.ExpandTreeCommand
import io.micronaut.configuration.picocli.PicocliRunner
import picocli.CommandLine.Command
import picocli.CommandLine.Option

@Command(
    name = "wikipedia-cli",
    description = ["Command line interface to search Wikipedia"],
    mixinStandardHelpOptions = true,
    subcommands = [ExpandTreeCommand::class],
    version = ["1.0.0    Launch version"]
)
class WikipediaCliCommand : Runnable {

    @Option(
        names = ["-h", "--help"],
        usageHelp = true,
        description = ["Shows this help message and quits."]
    )
    var usageHelpRequested = false

    @Option(
        names = ["-V", "--version"],
        versionHelp = true,
        description = ["Prints version and quits."]
    )
    var versionRequested = false

    override fun run() {
        // Implemented in subcommands.
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            PicocliRunner.run(WikipediaCliCommand::class.java, *args)
        }
    }
}
