package org.smarthata.alice.rest;

import org.slf4j.LoggerFactory
import org.smarthata.alice.model.auth.AccessToken
import org.smarthata.alice.model.auth.LoginInfo
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import java.util.*

@Controller
@RequestMapping("/alice/oauth")
class AuthController(
    @Value("\${oauth2.0.password}") private val oauthPassword: String,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @RequestMapping(value = ["/login"], method = [RequestMethod.GET])
    fun login(
        @RequestParam("scope") scope: String,
        @RequestParam("state") state: String,
        @RequestParam("redirect_uri") redirectUri: String,
        @RequestParam("response_type") responseType: String,
        @RequestParam("client_id") clientId: String,
        model: Model
    ): String {

        logger.info("Login page")

        val loginInfo = LoginInfo(
            scope = scope,
            state = state,
            redirectUri = redirectUri,
            responseType = responseType,
            clientId = clientId,
        )
        model.addAttribute("loginInfo", loginInfo)
        if (clientId != "yandex-alice") {
            logger.warn("Wrong client_id $clientId")
            loginInfo.errorMessage = "Wrong client_id $clientId"
            loginInfo.showSubmitButton = false
        }
        return "login"
    }

    @RequestMapping(value = ["/auth"], method = [RequestMethod.POST])
    fun auth(
        @RequestParam("scope") scope: String,
        @RequestParam("state") state: String,
        @RequestParam("redirect_uri") redirectUri: String,
        @RequestParam("response_type") responseType: String,
        @RequestParam("client_id") clientId: String,
        @RequestParam("password") password: String,
        model: Model
    ): String {

        logger.info("Auth page")

        val loginInfo = LoginInfo(
            scope = scope,
            state = state,
            redirectUri = redirectUri,
            responseType = responseType,
            clientId = clientId,
        )
        model.addAttribute("loginInfo", loginInfo)

        if (password != oauthPassword) {
            logger.info("Wrong password")
            loginInfo.errorMessage = "Wrong password"
            return "login"
        }

        loginInfo.code = UUID.randomUUID().toString()
        logger.info("Generate code $loginInfo")

        return "auth"
    }

    @RequestMapping("/token")
    @ResponseBody
    fun token(
        @RequestParam("code") code: String?,
        @RequestParam("client_secret") clientSecret: String?,
        @RequestParam("grant_type") grantType: String,
        @RequestParam("client_id") clientId: String?,
        @RequestParam("redirect_uri") redirectUri: String?,
    ): AccessToken {
        logger.info("Get token code = [${code}], clientSecret = [${clientSecret}], grantType = [${grantType}], clientId = [${clientId}], redirectUri = [${redirectUri}]")

        return AccessToken(
            accessToken = UUID.randomUUID().toString(),
            tokenType = grantType,
            expiresIn = 3600,
            refreshToken = UUID.randomUUID().toString()
        )
    }
}
