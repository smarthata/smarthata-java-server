package org.smarthata.service.tm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smarthata.service.message.SmarthataMessage;
import org.smarthata.service.message.SmarthataMessageBroker;
import org.smarthata.service.message.SmarthataMessageListener;
import org.smarthata.service.tm.command.Command;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.smarthata.service.message.SmarthataMessage.SOURCE_TM;

@Service
public class TmBot extends TelegramLongPollingBot implements SmarthataMessageListener {

    private static final Logger LOG = LoggerFactory.getLogger(TmBot.class);

    @Value("${bot.token}")
    private String token;

    @Value("${bot.username}")
    private String username;

    @Value("${bot.adminChatId}")
    private Long adminChatId;

    private final Map<String, Command> commandsMap;

    @Autowired
    private SmarthataMessageBroker messageBroker;

    @Autowired
    public TmBot(List<Command> commands) {
        commandsMap = commands.stream()
                .collect(Collectors.toMap(Command::getCommand, Function.identity()));
    }


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

        LOG.debug("Received update: {}", update);

        if (update.hasMessage()) {
            Message message = update.getMessage();
            onMessageReceived(message.getChatId(), message.getText());
        }

        if (update.hasCallbackQuery()) {
            CallbackQuery callback = update.getCallbackQuery();
            onMessageReceived(callback.getMessage().getChatId(), callback.getData(), callback.getMessage().getMessageId());
        }

    }

    @Override
    public void receiveSmarthataMessage(SmarthataMessage message) {
        if (!SOURCE_TM.equalsIgnoreCase(message.getSource())) {
            if (message.getPath().equals("/messages")) {
                processMessage(adminChatId, message.getPath() + "/" + message.getText(), null);
            }
        }
    }

    private void onMessageReceived(Long chatId, String text) {
        onMessageReceived(chatId, text, null);
    }

    private void onMessageReceived(Long chatId, String text, Integer messageId) {

        LOG.info("text: [{}], messageId {}", text, messageId);
        text = text.replace("@" + username, "");

        if (!text.isEmpty()) {
            broadcastSmarthataMessage(text);
        }

        processMessage(chatId, text, messageId);
    }

    private void processMessage(Long chatId, String text, Integer messageId) {
        List<String> path = Arrays.stream(text.split("/"))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        LOG.info("Process telegram message: path {}, text: {}", path, text);
        if (path.isEmpty()) return;

        String commandName = path.remove(0);
        Command command = commandsMap.get(commandName);
        if (command != null) {
            LOG.info("Found command: [{}]", commandName);
            BotApiMethod<?> botApiMethod = command.answer(path, chatId.toString(), messageId);
            sendMessageToTelegram(botApiMethod);
        }
    }

    private void sendMessageToTelegram(BotApiMethod<?> botApiMethod) {
        try {
            LOG.info("Try to send message to telegram: {}", botApiMethod);
            super.execute(botApiMethod);
            LOG.info("Message to telegram sent: {}", botApiMethod);
        } catch (TelegramApiException e) {
            LOG.error("Telegram Api Exception: {}", e.getMessage(), e);
        }
    }

    private void broadcastSmarthataMessage(String text) {
        SmarthataMessage message;
        if (text.matches("\\s")) {
            String[] split = text.split("\\s", 2);
            message = new SmarthataMessage(split[0], split[1], SOURCE_TM);
        } else {
            message = new SmarthataMessage(text, "", SOURCE_TM);
        }
        messageBroker.broadcastSmarthataMessage(message);
    }
}
