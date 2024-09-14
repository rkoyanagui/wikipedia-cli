package com.rkoyanagui.wikipediacli.domain

class PrintedNode<T>(val node: Node<T>, var acc: MutableList<String>, var depth: Int)
