package org.smarthata.alice.model.smarthome

data class State(
    val instance: String,
    val value: Boolean? = null,
)