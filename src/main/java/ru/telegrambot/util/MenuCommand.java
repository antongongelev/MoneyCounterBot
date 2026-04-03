package ru.telegrambot.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
@AllArgsConstructor
public enum MenuCommand {

    NEW_EXPENSE("\uD83C\uDD95 Добавить  расход"),
    LIST_OF_EXPENSES("\uD83D\uDCD6 Список расходов"),
    CLOSE("❌ Закрыть меню"),
    ;

    private final String menuButtonText;

    public static Optional<MenuCommand> of(String value) {
        return Arrays.stream(values())
                .filter(e -> e.name().equals(value))
                .findFirst();
    }

}
