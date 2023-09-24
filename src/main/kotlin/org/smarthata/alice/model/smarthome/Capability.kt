package org.smarthata.alice.model.smarthome

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
open class Capability(
    open val type: String,
    open val state: State?,
)

data class OnOffCapability(
    override val type: String = "devices.capabilities.on_off",
    override val state: State? = null
) : Capability(type, state)
