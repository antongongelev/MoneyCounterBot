package ru.telegrambot.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

@Service
public class UserService {

    public Pair<Long, String> getUserFromMessage(Message message) {
        String playerName;
        User user = message.getFrom();

        Long userID = user.getId();
        String firstName = user.getFirstName();
        String lastName = user.getLastName();
        String userName = user.getUserName();

        if (StringUtils.isNotEmpty(firstName) && StringUtils.isNotEmpty(lastName)) {
            playerName = firstName + " " + lastName;
        } else if (StringUtils.isNotEmpty(userName)) {
            playerName = userName;
        } else if (StringUtils.isNotEmpty(firstName)) {
            playerName = firstName;
        } else {
            playerName = lastName;
        }

        String s1 = playerName.replaceAll("_", "-");
        String s2 = s1.replaceAll("@", "-");
        String formattedName = s2.replaceAll("&", "-");

        return Pair.of(userID, formattedName);
    }

}
