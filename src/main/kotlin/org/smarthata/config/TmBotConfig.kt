package org.smarthata.config

import org.slf4j.LoggerFactory
import org.smarthata.service.tm.TmBot
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession

@Configuration
@ConditionalOnProperty(value = ["bot.enabled"], matchIfMissing = true)
class TmBotConfig {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Bean
    fun tmBotApi(bot: TmBot): TelegramBotsApi? {
        return try {
            TelegramBotsApi(DefaultBotSession::class.java).apply {
                registerBot(bot)
            }
        } catch (e: TelegramApiException) {
            logger.error("Bot creation failed: {}", e.message, e)
            null
        }
    }
}
