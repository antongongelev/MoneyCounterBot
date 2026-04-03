package ru.telegrambot.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.telegrambot.configuration.PropertyStorage;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ChatStateStorage {

    private final PropertyStorage propertyStorage;

    private final Map<Long, PropertyStorage.Chat> chats = new HashMap<>();

    @PostConstruct
    private void initialize() {
        propertyStorage.getChats().forEach(chat -> {
            String chatId = chat.getChatId();
            chats.put(Long.valueOf(chatId), chat);
        });
    }

    public Optional<PropertyStorage.Chat> getChatById(Long chatId) {
        return Optional.ofNullable(chats.get(chatId));
    }

}
