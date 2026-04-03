package ru.telegrambot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.telegrambot.entity.MoneyChat;

import java.util.Optional;

public interface MoneyChatRepository extends JpaRepository<MoneyChat, Long> {

    Optional<MoneyChat> findByName(String name);

}
