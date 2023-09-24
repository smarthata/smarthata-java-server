package org.smarthata.alice.model.auth

data class AccessToken(
    val accessToken: String,
    val tokenType: String,
    val expiresIn: Int,
    val refreshToken: String,
)