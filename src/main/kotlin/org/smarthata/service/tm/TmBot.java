package org.smarthata.service.tm;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.smarthata.service.message.EndpointType;
import org.smarthata.service.message.SmarthataMessage;
import org.smarthata.service.message.SmarthataMessageBroker;
import org.smarthata.service.message.SmarthataMessageListener;
import org.smarthata.service.tm.command.Command;
import org.smarthata.service.tm.command.CommandRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static org.smarthata.service.message.EndpointType.TELEGRAM;

@Service
@Slf4j
@ConditionalOnProperty(value = "bot.enabled", matchIfMissing = true)
public class TmBot extends TelegramLongPollingBot implements SmarthataMessageListener {

    @Value("${bot.username}")
    private String username;

    @Value("${bot.adminChatId}")
    private String adminChatId;

    private final Map<String, Command> commandsMap;

    private final SmarthataMessageBroker messageBroker;

    @Autowired
    public TmBot(
            @Value("${bot.token}") String token,
            List<Command> commands,
            SmarthataMessageBroker messageBroker
    ) {
        super(token);
        this.messageBroker = messageBroker;
        this.messageBroker.register(this);

        commandsMap = commands.stream()
                .collect(Collectors.toMap(Command::getCommand, Function.identity()));
    }

    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public void onUpdateReceived(final Update update) {

        log.debug("Received update: {}", update);

        if (update.hasMessage()) {
            Message message = update.getMessage();
            onMessageReceived(message.getChatId(), message.getText());
        }

        if (update.hasCallbackQuery()) {
            CallbackQuery callback = update.getCallbackQuery();
            onMessageReceived(callback.getMessage().getChatId(), callback.getData(),
                    callback.getMessage().getMessageId());
        }

    }

    @Override
    public void receiveSmarthataMessage(SmarthataMessage message) {
        if (message.getPath().equals("/messages")) {
            BotApiMethod<?> botApiMethod = new SendMessage(adminChatId, message.getText());
            sendMessageToTelegram(botApiMethod);
        }
    }

    @Override
    public EndpointType getEndpointType() {
        return TELEGRAM;
    }

    private void onMessageReceived(Long chatId, String text) {
        onMessageReceived(chatId, text, null);
    }

    private void onMessageReceived(Long chatId, String text, Integer messageId) {

        log.info("text: [{}], messageId {}", text, messageId);
        if (text == null) {
            return;
        }

        text = text.replace("@" + username, "");

        boolean messageProcessed = processMessage(chatId, text, messageId);

        if (!messageProcessed && !text.isEmpty()) {
            broadcastSmarthataMessage(text);
        }
    }

    private boolean processMessage(Long chatId, String text, Integer messageId) {
        List<String> path = getPath(text);
        log.info("Process telegram message: path {}, text: {}", path, text);
        if (path.isEmpty()) {
            path.add("");
        }

        String commandName = path.remove(0);
        Command command = commandsMap.get(commandName);
        if (command != null) {
            log.info("Found command: [{}]", commandName);
            BotApiMethod<?> botApiMethod = command.answer(new CommandRequest(path, chatId.toString(), messageId));
            sendMessageToTelegram(botApiMethod);
            return true;
        }
        return false;
    }

    private List<String> getPath(String text) {
        return Arrays.stream(text.split("/"))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    public boolean sendMessageToTelegram(BotApiMethod<?> botApiMethod) {
        try {
            super.execute(botApiMethod);
            log.debug("Message to telegram sent: {}", botApiMethod);
            return true;
        } catch (TelegramApiException e) {
            log.error("Telegram Api Exception: {}", e.getMessage(), e);
        }
        return false;
    }

    private void broadcastSmarthataMessage(String text) {
        SmarthataMessage message;
        if (text.matches("\\s")) {
            String[] split = text.split("\\s", 2);
            message = new SmarthataMessage(split[0], split[1], TELEGRAM);
        } else {
            message = new SmarthataMessage(text, "", TELEGRAM);
        }
        messageBroker.broadcastSmarthataMessage(message);
    }
}
