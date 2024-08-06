package org.smarthata.alice.model.smarthome

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
open class Capability<T>(
    open val type: String,
    open val state: State<T>?,
)


const val DEVICES_CAPABILITIES_ON_OFF = "devices.capabilities.on_off"

@JsonInclude(JsonInclude.Include.NON_NULL)
data class OnOffCapability(
    override val type: String = DEVICES_CAPABILITIES_ON_OFF,
    override val state: State<Boolean>? = null,
) : Capability<Boolean>(type, state)


const val DEVICES_CAPABILITIES_RANGE = "devices.capabilities.range"

@JsonInclude(JsonInclude.Include.NON_NULL)
data class RangeCapability(
    override val type: String = DEVICES_CAPABILITIES_RANGE,
    override val state: State<Double>? = null,
    val parameters: RangeParameter,
) : Capability<Double>(type, state)
