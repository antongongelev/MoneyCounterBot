package ru.telegrambot.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
@AllArgsConstructor
public enum MenuCommand {

    NEW_EXPENSE("➕ Добавить расход"),
    LIST_OF_EXPENSES("📊 Список расходов"),
    BACK_TO_MENU("◀️ Назад в меню"),
    APPLY_DELETION("✅ Да, удалить"),
    CANCEL_DELETION("❌ Нет, отменить"),
    CLOSE("❌ Закрыть меню"),
    ;

    private final String menuButtonText;

    public static Optional<MenuCommand> of(String value) {
        return Arrays.stream(values())
                .filter(e -> e.name().equals(value))
                .findFirst();
    }

}
