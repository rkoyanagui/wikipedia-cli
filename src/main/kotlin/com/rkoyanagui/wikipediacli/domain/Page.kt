package com.rkoyanagui.wikipediacli.domain

data class Page(val titles: Titles, val description: String? = null) {

    override fun toString(): String {
        return "{\"title\":\"$titles\",\"description\":\"$description\"}"
    }
}