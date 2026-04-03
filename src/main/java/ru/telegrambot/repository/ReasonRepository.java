package ru.telegrambot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.telegrambot.entity.Reason;

import java.util.Optional;

public interface ReasonRepository extends JpaRepository<Reason, Long> {

    Optional<Reason> findByName(String name);

}
