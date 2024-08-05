package org.smarthata.alice.model.smarthome

open class State<T>(
    open val instance: String,
    open val value: Any,
)

data class BooleanState(
    override val instance: String,
    override val value: Boolean,
) : State<Boolean>(instance, value)


data class FloatState(
    override val instance: String,
    override val value: Double,
) : State<Double>(instance, value)
