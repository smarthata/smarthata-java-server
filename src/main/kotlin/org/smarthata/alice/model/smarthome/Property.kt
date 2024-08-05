package org.smarthata.alice.model.smarthome

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
open class Property(
    open val type: String,
    open val retrievable: Boolean,
    open val reportable: Boolean,
    open val parameters: Parameter? = null,
    open val state: State<*>? = null,
    @JsonProperty("last_updated")
    open val lastUpdated: LocalDateTime? = null,
)


interface Parameter {
    val instance: String
    val unit: String
}

data class TempParameter(
    override val instance: String = "temperature",
    override val unit: String = "unit.temperature.celsius",
) : Parameter
