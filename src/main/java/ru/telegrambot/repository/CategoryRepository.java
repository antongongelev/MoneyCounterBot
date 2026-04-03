package ru.telegrambot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import ru.telegrambot.entity.Category;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Integer> {

    List<Category> findAllByMoneyChatId(Long moneyChatId);

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    void deleteAllByMoneyChatIdAndName(Long moneyChatId, String name);

}
