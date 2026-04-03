package ru.telegrambot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import ru.telegrambot.entity.Expense;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findAllByMoneyChatName(String chatName);

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    void deleteAllByMoneyChatName(String chatName);

}
