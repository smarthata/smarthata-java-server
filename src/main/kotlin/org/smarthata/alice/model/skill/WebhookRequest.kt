package org.smarthata.alice.model.skill

data class WebhookRequest(
    val request: Request
) {
    data class Request(
        val command: String,
        val originalUtterance: String?,
        val type: String,
    )
}
