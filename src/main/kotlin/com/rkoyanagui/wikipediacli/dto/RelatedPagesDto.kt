package com.rkoyanagui.wikipediacli.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import io.micronaut.core.annotation.Introspected

@Introspected
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class RelatedPagesDto(var pages: Array<PageDto>) {

    override fun toString(): String {
        return "{\"pages\":$pages}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RelatedPagesDto

        if (!pages.contentEquals(other.pages)) return false

        return true
    }

    override fun hashCode(): Int {
        return pages.contentHashCode()
    }
}
