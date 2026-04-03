package ru.telegrambot.service;

import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;

@Data
public class ExpenseDialogState {

    private Long chatId;
    private DialogStep currentStep;
    private Double amount;
    private String category;
    private Pair<Long, String> user;

    public enum DialogStep {
        WAITING_FOR_AMOUNT,
        WAITING_FOR_CATEGORY,
    }
}