package org.smarthata.alice.service.notification

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.smarthata.alice.model.smarthome.Device
import org.smarthata.alice.model.smarthome.DevicesPayload
import org.smarthata.alice.model.smarthome.FloatState
import org.smarthata.alice.model.smarthome.Property
import org.smarthata.alice.model.smarthome.notification.NotificationRequest
import org.smarthata.alice.model.smarthome.notification.NotificationResponse
import org.smarthata.service.WeatherService
import org.smarthata.service.device.Room
import org.smarthata.service.device.heating.HeatingService
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class AliceTempNotificator(
    @Value("\${alice.url}") url: String,
    @Value("\${alice.token}") token: String,
    private var heatingService: HeatingService,
    private var weatherService: WeatherService,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val client: WebClient = WebClient.builder()
        .baseUrl(url)
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .defaultHeader(HttpHeaders.AUTHORIZATION, "OAuth $token")
        .build()

    private val updates: MutableMap<String, Double> = mutableMapOf()

    @Scheduled(fixedDelay = 60000)
    fun notifyAlice() {

        logger.info("Alice device parameters try update")

        val devices = listOf("bedroom", "bathroom", "garage", "workshop", "street")

        val payload = DevicesPayload(
            userId = "valery",
            devices = devices.map { createDevice(deviceId = it) }
                .filter {
                    val currentValue = it.properties.first().state!!.value as Double
                    currentValue != updates.put(it.id, currentValue) && currentValue > -50
                }
        )
        logger.info("Alice device parameters update request: {}", jacksonObjectMapper().writeValueAsString(payload))
        if (payload.devices.isNotEmpty()) {
            val response = postUpdates(payload)
            logger.info("Alice device parameters update response: {}", response)
        } else {
            logger.info("Alice device parameters update skip due to empty changed devices")
        }
    }

    private fun createDevice(deviceId: String) = Device(
        id = "temp-$deviceId",
        properties = listOf(Property(
            type = "devices.properties.float",
            state = floatState(Room.getFromRoomCode(deviceId))
        ))
    )

    private fun floatState(room: Room) =
        FloatState("temperature",
            if (room == Room.STREET)
                weatherService.getLastStreetTemp()
            else
                heatingService.actualTemp(room)
        )

    private fun postUpdates(payload: DevicesPayload) =
        client.post()
            .uri("/callback/state")
            .bodyValue(NotificationRequest(payload = payload))
            .retrieve()
            .bodyToMono(NotificationResponse::class.java).block()
}
