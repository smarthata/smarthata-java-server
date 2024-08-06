package org.smarthata.alice.model.smarthome

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Device(
    val id: String,
    val name: String?,
    val room: String?,
    val type: String?,
    val capabilities: List<Capability<*>> = listOf(),
    val properties: List<Property> = listOf(),
    val errorCode: String? = null,
    val errorMessage: String? = null,
)

