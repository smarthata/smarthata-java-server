package org.smarthata.alice.model.smarthome

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Property(
    val type: String,
    val retrievable: Boolean? = null,
    val reportable: Boolean? = null,
    val parameters: Parameter? = null,
    val state: State<*>? = null,
    @JsonProperty("last_updated")
    val lastUpdated: LocalDateTime? = null,
)
