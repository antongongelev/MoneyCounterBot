package ru.telegrambot.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Configuration
@ConfigurationProperties(value = "domain", ignoreInvalidFields = true)
public class PropertyStorage {

    @Value("${domain.bot.name}")
    private String name;

    @Value("${domain.bot.token}")
    private String token;

    @NestedConfigurationProperty
    private final List<Chat> chats = new ArrayList<>();

    @Getter
    @Setter
    public static class Chat {
        private String name;
        private String chatId;
    }

}
