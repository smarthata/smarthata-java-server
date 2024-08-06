package org.smarthata.alice.model.smarthome

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
open class State<T>(
    open val instance: String,
    open val value: Any,
    open val actionResult: ActionResult?,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class BooleanState(
    override val instance: String,
    override val value: Boolean,
    override val actionResult: ActionResult? = null,
) : State<Boolean>(instance, value, actionResult)


@JsonInclude(JsonInclude.Include.NON_NULL)
data class FloatState(
    override val instance: String,
    override val value: Double,
    override val actionResult: ActionResult? = null,
) : State<Double>(instance, value, actionResult)
