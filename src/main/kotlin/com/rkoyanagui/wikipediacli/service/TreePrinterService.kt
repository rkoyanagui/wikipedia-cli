package com.rkoyanagui.wikipediacli.service

import com.rkoyanagui.wikipediacli.domain.Node
import com.rkoyanagui.wikipediacli.domain.Page
import com.rkoyanagui.wikipediacli.domain.PrintedNode
import jakarta.inject.Singleton
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Singleton
class TreePrinterService {

    fun formatTree(tree: Node<Page>): Mono<String> {
        return expandTree(PrintedNode(tree, mutableListOf(), 0))
            .map { node -> connectList(node.acc) }
            .defaultIfEmpty("empty")
    }

    private fun connectList(list: MutableList<String>): String {
        for (i in 3 until list.size) {
            val line = list[i]
            val pipeIndex = line.lastIndexOf("|")
            for (j in i - 1 downTo 2) {
                val otherLine = list[j]
                val originalChar = otherLine.substring(pipeIndex, pipeIndex + 1)
                if (originalChar != " ") {
                    break
                }
                val rewrittenLine = otherLine.replaceRange(pipeIndex, pipeIndex + 1, "|")
                list[j] = rewrittenLine
            }
        }
        return list.reduce { a, b -> a + "\n" + b }
    }

    private fun expandTree(tree: PrintedNode<Page>): Mono<PrintedNode<Page>> {
        return Mono.just(tree)
            .map { printedNode -> appendNode(printedNode) }
            .flatMapMany { parent ->
                Flux.fromIterable(parent.node.children.values)
                    .map { child -> PrintedNode(child, parent.acc, parent.depth) }
                    .flatMap { child -> expandTree(child) }
            }.then(Mono.just(tree))
    }

    private fun appendNode(pn: PrintedNode<Page>): PrintedNode<Page> {
        var txt = ""
        if (pn.depth > 0) {
            txt = "|- "
        }
        txt += pn.node.value.titles.canonical
        val line = indent(txt, pn.depth)
        pn.acc.add(line)
        pn.depth += 1
        return pn
    }

    private fun indent(str: String, level: Int): String {
        var acc = ""
        for (i in 1 until level) {
            val indentation = "   "
            acc += indentation
        }
        return acc + str
    }
}
