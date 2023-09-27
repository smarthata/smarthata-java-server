package org.smarthata.rest

import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class PingController {
    @Value("\${application.name}")
    private val applicationName: String? = null

    @Value("\${build.version}")
    private val buildVersion: String? = null

    @GetMapping("/")
    fun version(): String = "$applicationName:$buildVersion"
}