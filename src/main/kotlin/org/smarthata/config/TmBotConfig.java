package org.smarthata.config;

import lombok.extern.slf4j.Slf4j;
import org.smarthata.service.tm.TmBot;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
@Slf4j
@ConditionalOnProperty(value = "bot.enabled", matchIfMissing = true)
public class TmBotConfig {

    @Bean
    public TelegramBotsApi tmBotApi(TmBot bot) {
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(bot);
            return telegramBotsApi;
        } catch (TelegramApiException e) {
            log.error("Bot creation failed: {}", e.getMessage(), e);
        }
        return null;
    }

}
