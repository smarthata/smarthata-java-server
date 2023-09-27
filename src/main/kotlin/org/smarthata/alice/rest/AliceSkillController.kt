package org.smarthata.alice.rest

import org.smarthata.alice.model.skill.Directives
import org.smarthata.alice.model.skill.WebhookRequest
import org.smarthata.alice.model.skill.WebhookResponse
import org.smarthata.alice.model.skill.WebhookResponse.Response
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/alice")
class AliceSkillController {
    @PostMapping
    fun alice(@RequestBody body: WebhookRequest): WebhookResponse {
        return WebhookResponse(Response("text", Directives(Directives.StartAccountLinking)))
    }
}
