package com.rkoyanagui.wikipediacli.domain

import java.util.*

class Node<T>(
    val value: T,
    children: MutableMap<String, Node<T>> = LinkedHashMap<String, Node<T>>()
) {

    val children: MutableMap<String, Node<T>> = Collections.synchronizedMap(children)

    @Synchronized
    fun addChildren(children: Map<String, Node<T>>): Node<T> {
        this.children.putAll(children)
        return this
    }

    override fun toString(): String = "{\"value\":$value,\"children\":${children.values}}"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Node<*>

        if (value != other.value) return false
        if (children != other.children) return false

        return true
    }

    override fun hashCode(): Int {
        var result = value?.hashCode() ?: 0
        result = 31 * result + children.hashCode()
        return result
    }
}
