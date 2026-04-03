package ru.telegrambot.service;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.telegrambot.bot.TelegramBot;
import ru.telegrambot.configuration.PropertyStorage;
import ru.telegrambot.util.MarkupBuilder;
import ru.telegrambot.util.MenuCommand;

import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MoneyCounterService {

    @Setter
    private TelegramBot telegramBot;

    private final MarkupBuilder markupBuilder;
    private final ChatStateStorage stateStorage;

    public void onUpdate(Update update) {
        if (update.hasCallbackQuery()) {
            handleCallbackQuery(update);
            return;
        }

        Message message = update.getMessage();
        if (Objects.isNull(message) || !message.hasText()) {
            return;
        }

        Long chatId = message.getChatId();
        String messageText = message.getText();

        Optional<PropertyStorage.Chat> chat = stateStorage.getChatById(chatId);
        if (!chat.isPresent()) {
            sendMessage(chatId, "Этот бот не предназначен для данного чата");
            return;
        }

        if (messageText.equals("/start")) {
            showMainMenu(chatId);
        }
    }

    private void handleCallbackQuery(Update update) {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        Optional<MenuCommand> command = MenuCommand.of(update.getCallbackQuery().getData());

        if (!command.isPresent()) {
            sendMessage(chatId, "Неизвестная команда");
            return;
        }

        switch (command.get()) {
            case LIST_OF_EXPENSES:
                editMessageText(chatId, messageId, "Список 🎉", null);
                break;
            case NEW_EXPENSE:
                editMessageText(chatId, messageId, "Новая трата 🚀", null);
                break;
            case CLOSE:
                deleteMessage(chatId, messageId);
                break;
        }
    }

    private void showMainMenu(Long chatId) {
        String menuText = "🏠 *Меню*\n\nЧто Вы хотите сделать?";
        sendMessage(chatId, menuText, markupBuilder.mainMenu());
    }

    private void sendMessage(Long chatId, String message) {
        sendMessage(chatId, message, null);
    }

    private void sendMessage(Long chatId, String message, InlineKeyboardMarkup keyboardMarkup) {
        if (StringUtils.isBlank(message)) {
            return;
        }

        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(message);
        Optional.ofNullable(keyboardMarkup).ifPresent(sendMessage::setReplyMarkup);

        try {
            telegramBot.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Can't send telegram message", e);
        }
    }

    private void editMessageText(Long chatId, Integer messageId, String text, InlineKeyboardMarkup keyboard) {
        if (StringUtils.isBlank(text)) {
            return;
        }

        // Edit the message text
        org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText editMessage =
                new org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText();
        editMessage.enableMarkdown(true);
        editMessage.setChatId(String.valueOf(chatId));
        editMessage.setMessageId(messageId);
        editMessage.setText(text);

        try {
            telegramBot.execute(editMessage);
        } catch (TelegramApiException e) {
            log.error("Can't edit message", e);
        }

        // Edit the keyboard if provided
        if (keyboard != null) {
            org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup editMarkup =
                    new org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup();
            editMarkup.setChatId(String.valueOf(chatId));
            editMarkup.setMessageId(messageId);
            editMarkup.setReplyMarkup(keyboard);

            try {
                telegramBot.execute(editMarkup);
            } catch (TelegramApiException e) {
                log.error("Can't edit keyboard", e);
            }
        }
    }

    private void deleteMessage(Long chatId, Integer messageId) {
        org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage deleteMessage =
                new org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage();
        deleteMessage.setChatId(String.valueOf(chatId));
        deleteMessage.setMessageId(messageId);

        try {
            telegramBot.execute(deleteMessage);
        } catch (TelegramApiException e) {
            log.error("Can't delete message", e);
        }
    }
}