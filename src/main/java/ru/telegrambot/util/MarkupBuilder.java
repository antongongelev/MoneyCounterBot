package ru.telegrambot.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@Service
public class MarkupBuilder {

    public InlineKeyboardMarkup mainMenu() {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        // First row - two buttons
        List<InlineKeyboardButton> row1 = new ArrayList<>();

        row1.add(of(MenuCommand.NEW_EXPENSE));
        row1.add(of(MenuCommand.LIST_OF_EXPENSES));
        keyboard.add(row1);

        // Second row - navigation buttons
        List<InlineKeyboardButton> row2 = new ArrayList<>();

        row2.add(of(MenuCommand.CLOSE));
        keyboard.add(row2);

        markup.setKeyboard(keyboard);
        return markup;
    }

    public InlineKeyboardMarkup expensesListMenu() {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();

        row1.add(of(MenuCommand.BACK_TO_MENU));
        keyboard.add(row1);

        markup.setKeyboard(keyboard);
        return markup;
    }

    public InlineKeyboardMarkup expenseRemovalMenu(Integer expenseId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row = new ArrayList<>();

        row.add(of(MenuCommand.APPLY_DELETION, "_" + expenseId));
        row.add(of(MenuCommand.CANCEL_DELETION));
        keyboard.add(row);

        markup.setKeyboard(keyboard);
        return markup;
    }

    private InlineKeyboardButton of(MenuCommand menuCommand) {
        return of(menuCommand, StringUtils.EMPTY);
    }

    private InlineKeyboardButton of(MenuCommand menuCommand, String payload) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(menuCommand.getMenuButtonText());
        button.setCallbackData(menuCommand.name() + payload);
        return button;
    }
}