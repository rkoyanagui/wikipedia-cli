package com.rkoyanagui.wikipediacli.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import io.micronaut.core.annotation.Introspected

@Introspected
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class TitlesDto(
    var canonical: String,
    @JsonProperty("normalized") var normalised: String
) {
    override fun toString(): String {
        return normalised
    }
}
