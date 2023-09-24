package org.smarthata.alice.rest

import lombok.extern.slf4j.Slf4j
import org.smarthata.alice.model.skill.Directives
import org.smarthata.alice.model.skill.WebhookRequest
import org.smarthata.alice.model.skill.WebhookResponse
import org.smarthata.alice.model.skill.WebhookResponse.Response
import org.springframework.web.bind.annotation.*

@Slf4j
@RestController
@RequestMapping("/alice")
class AliceSkillController {
    @PostMapping
    fun alice(@RequestBody body: WebhookRequest): WebhookResponse {
        return WebhookResponse(Response("text", Directives(Directives.StartAccountLinking)))
    }
}
