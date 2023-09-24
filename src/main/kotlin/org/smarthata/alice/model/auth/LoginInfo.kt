package org.smarthata.alice.model.auth

import java.net.URLEncoder
import java.nio.charset.StandardCharsets.UTF_8

data class LoginInfo(
    val scope: String,
    val state: String,
    val redirectUri: String,
    val responseType: String,
    val clientId: String,
    var code: String? = null,
    var errorMessage: String? = null,
    var showSubmitButton: Boolean = true,
) {
    fun toUriParams(): String = "auth?scope=${encode(scope)}" +
        "&state=${encode(state)}" +
        "&redirect_uri=${encode(redirectUri)}" +
        "&response_type=${encode(responseType)}" +
        "&client_id=${encode(clientId)}"

    fun makeRedirectUri(): String {
        return redirectUri +
            "?code=$code" +
            "&state=${encode(state)}" +
            "&client_id=$clientId" +
            "&scope=${encode(scope)}"
    }

    private fun encode(s: String): String = URLEncoder.encode(s, UTF_8.toString())
}