package org.smarthata.alice.model.smarthome

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ActionResult(
    val status: String? = null,
    val errorCode: String? = null,
    val errorMessage: String? = null,
)
