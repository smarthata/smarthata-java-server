package org.smarthata.alice.model.smarthome

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
open class Capability<T>(
    open val type: String,
    open val state: State<T>?,
)


const val DEVICES_CAPABILITIES_ON_OFF = "devices.capabilities.on_off"
data class OnOffCapability(
    override val type: String = DEVICES_CAPABILITIES_ON_OFF,
    override val state: State<Boolean>? = null
) : Capability<Boolean>(type, state)
