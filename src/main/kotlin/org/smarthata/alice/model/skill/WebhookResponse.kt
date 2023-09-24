package org.smarthata.alice.model.skill

data class WebhookResponse(
    val response: Response,
    val version: String = "1.0",
) {
    data class Response(
        val text: String? = null,
        val directives: Directives? = null,
    )
}

data class Directives(
    val startAccountLinking: StartAccountLinking?
) {
    object StartAccountLinking
}