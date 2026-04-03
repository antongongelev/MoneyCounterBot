package ru.telegrambot.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Service;
import ru.telegrambot.configuration.PropertyStorage;
import ru.telegrambot.entity.MoneyChat;
import ru.telegrambot.repository.MoneyChatRepository;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ChatStateStorage {

    private final PropertyStorage propertyStorage;
    private final MoneyChatRepository moneyChatRepository;

    @Getter
    private final Map<Long, PropertyStorage.Chat> chats = new HashMap<>();

    @PostConstruct
    private void initialize() {
        propertyStorage.getChats().forEach(chat -> {
            String chatId = chat.getChatId();
            String chatName = chat.getName();
            chats.put(Long.valueOf(chatId), chat);

            if (!moneyChatRepository.findByName(chatName).isPresent()) {
                MoneyChat moneyChat = MoneyChat.builder()
                        .name(chatName)
                        .chatId(Long.valueOf(chatId))
                        .build();

                moneyChatRepository.save(moneyChat);
            }
        });
    }

    public Optional<PropertyStorage.Chat> getChatById(Long chatId) {
        return Optional.ofNullable(chats.get(chatId));
    }

}
