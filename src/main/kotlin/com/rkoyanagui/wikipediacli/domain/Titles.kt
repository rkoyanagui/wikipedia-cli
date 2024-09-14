package com.rkoyanagui.wikipediacli.domain

data class Titles(val canonical: String, val normalised: String) {

    override fun toString(): String {
        return normalised
    }
}
