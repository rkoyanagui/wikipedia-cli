package com.rkoyanagui.wikipediacli.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import io.micronaut.core.annotation.Introspected

@Introspected
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class PageDto(var titles: TitlesDto, var description: String? = null) {

    override fun toString(): String {
        return "{\"title\":\"$titles\",\"description\":\"$description\"}"
    }
}
