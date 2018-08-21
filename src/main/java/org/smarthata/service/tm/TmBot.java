package org.smarthata.service.tm;

import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smarthata.service.tm.command.Command;
import org.smarthata.service.tm.command.Temperatures;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TmBot extends TelegramLongPollingBot {

    private static final Logger LOG = LoggerFactory.getLogger(TmBot.class);

    @Value("${bot.token}")
    private String token;

    @Value("${bot.username}")
    private String username;

    @Value("${bot.adminChatId}")
    private Long adminChatId;

    @Autowired
    private List<Command> commands;

    @Autowired
    private Temperatures temperatures;


    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public void onUpdateReceived(final Update update) {
        try {
            LOG.debug("Received update: {}", update);

            if (update.hasMessage()) {
                Message message = update.getMessage();
                processMessage(message, message.getText());
            }

            if (update.hasCallbackQuery()) {
                CallbackQuery callback = update.getCallbackQuery();
                processMessage(callback.getMessage(), callback.getData(), callback.getMessage().getMessageId());
            }

        } catch (TelegramApiException e) {
            LOG.error("Telegram Api Exception", e);
        }
    }

    @Scheduled(cron = "0 0 9,10,13,17 * * *")
    public void sendStat() throws TelegramApiException {
        LOG.debug("Scheduling");
        super.execute(temperatures.answer(null, adminChatId.toString(), null));
    }

    private void processMessage(final Message message, String text) throws TelegramApiException {
        processMessage(message, text, null);
    }

    private void processMessage(final Message message, String text, final Integer messageId) throws TelegramApiException {

        LOG.info("text: '{}', messageId {}", text, messageId);
        text = text.replace("@" + username, "");

        List<String> path = Arrays.stream(text.split("/"))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        LOG.info("path: '{}'", path);
        if (path.isEmpty()) return;

        Command command = commands.stream()
                .filter(c -> c.isProcessed(path.get(0)))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Command not found"));

        path.remove(0);

        BotApiMethod botApiMethod = command.answer(path, message.getChatId().toString(), messageId);
        if (botApiMethod != null) {
            super.execute(botApiMethod);
        }
    }

    private ReplyKeyboardMarkup createMainButtons() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);
        replyKeyboardMarkup.setKeyboard(createKeyboardRows());
        return replyKeyboardMarkup;
    }

    private List<KeyboardRow> createKeyboardRows() {
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        keyboardFirstRow.add(new KeyboardButton("/temp"));
        keyboardFirstRow.add(new KeyboardButton("/heating"));
        keyboardFirstRow.add(new KeyboardButton("/watering"));
        keyboardFirstRow.add(new KeyboardButton("/lighting"));
        return ImmutableList.of(keyboardFirstRow);
    }

}
