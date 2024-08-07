package org.smarthata.alice.model.smarthome

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
open class Property(
    open val type: String,
    open val retrievable: Boolean? = null,
    open val reportable: Boolean? = null,
    open val parameters: Parameter? = null,
    open val state: State<*>? = null,
    @JsonProperty("last_updated")
    open val lastUpdated: LocalDateTime? = null,
)
