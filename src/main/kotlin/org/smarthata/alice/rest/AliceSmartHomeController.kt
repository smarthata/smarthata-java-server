package org.smarthata.alice.rest

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.smarthata.alice.model.smarthome.DeviceAction
import org.smarthata.alice.model.smarthome.DeviceQuery
import org.smarthata.alice.model.smarthome.DevicesPayload
import org.smarthata.alice.model.smarthome.DevicesResponse
import org.smarthata.alice.model.smarthome.Unlink
import org.smarthata.alice.service.AliceDevicesService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/alice/home/v1.0")
class AliceSmartHomeController(
    private val aliceDevicesService: AliceDevicesService,
    private val objectMapper: ObjectMapper,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @RequestMapping
    fun ping(): ResponseEntity<Any> {
        return ResponseEntity.ok().build()
    }

    @PostMapping("/user/unlink")
    fun unlink(
        @RequestHeader("Authorization") auth: String,
    ): Unlink {
        logger.info("Unlink auth: $auth")
        return Unlink(UUID.randomUUID().toString())
    }

    @GetMapping("/user/devices")
    fun devices(
        @RequestHeader("Authorization") auth: String?
    ): DevicesResponse {
        logger.info("Devices auth: $auth")
        return DevicesResponse(
            requestId = UUID.randomUUID().toString(),
            payload = DevicesPayload(
                userId = "valery",
                devices = aliceDevicesService.devices()
            )
        )
    }

    @PostMapping("/user/devices/query")
    fun devicesState(
        @RequestHeader("Authorization") auth: String,
        @RequestBody deviceQuery: DeviceQuery
    ): DevicesResponse {
        logger.info("Device auth: $auth")
        logger.info("Device query: $deviceQuery")
        return aliceDevicesService.devicesQuery(deviceQuery)
            .also { logger.info("Return query answer ${objectMapper.writeValueAsString(it)}") }
    }

    @PostMapping("/user/devices/action")
    fun devicesAction(
        @RequestHeader("Authorization") auth: String,
        @RequestBody deviceAction: DeviceAction
    ): DevicesResponse {
        logger.info("Action auth: $auth")
        logger.info("Device action: $deviceAction")
        return aliceDevicesService.devicesAction(deviceAction)
            .also { logger.info("Return action answer ${objectMapper.writeValueAsString(it)}") }
    }
}