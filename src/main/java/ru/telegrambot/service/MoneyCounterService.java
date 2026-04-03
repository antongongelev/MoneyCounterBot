package ru.telegrambot.service;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.telegrambot.bot.TelegramBot;
import ru.telegrambot.configuration.PropertyStorage;
import ru.telegrambot.entity.Category;
import ru.telegrambot.entity.Expense;
import ru.telegrambot.repository.CategoryRepository;
import ru.telegrambot.repository.ExpenseRepository;
import ru.telegrambot.util.MarkupBuilder;
import ru.telegrambot.util.MenuCommand;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MoneyCounterService {

    private static final double MAX_AMOUNT = 9_999_999.99;
    private static final int MAX_DECIMAL_PLACES = 2;
    private final UserService userService;

    @Setter
    private TelegramBot telegramBot;

    private final MarkupBuilder markupBuilder;
    private final ChatStateStorage stateStorage;
    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;

    private final Map<Long, ExpenseDialogState> chatStates = new ConcurrentHashMap<>();
    private final Map<Long, List<String>> chatCategories = new ConcurrentHashMap<>();

    @PostConstruct
    private void init() {
        stateStorage.getChats().keySet().forEach(chatId -> {
            List<String> categories = categoryRepository.findAllByMoneyChatId(chatId)
                    .stream()
                    .map(Category::getName)
                    .collect(Collectors.toList());

            chatCategories.put(chatId, categories);
        });
    }

    public void onUpdate(Update update) {
        if (update.hasCallbackQuery()) {
            handleCallbackQuery(update);
            return;
        }

        Message message = update.getMessage();
        if (Objects.isNull(message) || !message.hasText()) {
            return;
        }

        Long chatId = message.getChatId();
        String messageText = message.getText();

        Optional<PropertyStorage.Chat> chat = stateStorage.getChatById(chatId);
        if (!chat.isPresent()) {
            sendMessage(chatId, "Этот бот не предназначен для данного чата");
            return;
        }

        // Проверяем, есть ли активный диалог
        if (chatStates.containsKey(chatId)) {
            handleDialogInput(chatId, messageText);
        } else if (messageText.equals("/start")) {
            showMainMenu(chatId);
        } else {
            sendMessage(chatId, "Неизвестная команда. Используйте /start для начала работы");
        }
    }

    private void handleCallbackQuery(Update update) {
        Message message = update.getCallbackQuery().getMessage();
        Long chatId = message.getChatId();
        Integer messageId = message.getMessageId();

        Optional<MenuCommand> command = MenuCommand.of(update.getCallbackQuery().getData());

        if (!command.isPresent()) {
            sendMessage(chatId, "Неизвестная команда");
            return;
        }

        Pair<Long, String> user = userService.getUserFromMessage(message);

        switch (command.get()) {
            case LIST_OF_EXPENSES:
                editMessageText(chatId, messageId, "Список расходов будет здесь 🎉", null);
                break;
            case NEW_EXPENSE:
                startNewExpenseDialog(chatId, messageId, user);
                break;
            case CLOSE:
                deleteMessage(chatId, messageId);
                break;
        }
    }

    private void startNewExpenseDialog(Long chatId, Integer messageId, Pair<Long, String> user) {
        ExpenseDialogState dialog = new ExpenseDialogState();
        dialog.setChatId(chatId);
        dialog.setUser(user);
        dialog.setCurrentStep(ExpenseDialogState.DialogStep.WAITING_FOR_AMOUNT);
        chatStates.put(chatId, dialog);

        String text = "💰 *Новый расход*\n\nВведите сумму в рублях:";
        editMessageText(chatId, messageId, text, null);
    }

    private void handleDialogInput(Long chatId, String input) {
        ExpenseDialogState dialog = chatStates.get(chatId);

        if (dialog.getCurrentStep() == ExpenseDialogState.DialogStep.WAITING_FOR_AMOUNT) {
            handleAmountInput(chatId, dialog, input);
        } else if (dialog.getCurrentStep() == ExpenseDialogState.DialogStep.WAITING_FOR_CATEGORY) {
            handleCategoryInput(chatId, dialog, input);
        }
    }

    private void handleAmountInput(Long chatId, ExpenseDialogState dialog, String input) {
        // Проверяем, является ли число числом
        if (!isNumeric(input)) {
            sendMessage(chatId, "❌ Ошибка: Введите число!\n\nПожалуйста, введите сумму в рублях (например: 150.50):");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(input);
        } catch (NumberFormatException e) {
            sendMessage(chatId, "❌ Ошибка: Неверный формат числа!\n\nПожалуйста, введите сумму в рублях:");
            return;
        }

        // Проверяем положительное значение
        if (amount <= 0) {
            sendMessage(chatId, "❌ Ошибка: Сумма должна быть положительной!\n\nПожалуйста, введите сумму больше 0:");
            return;
        }

        // Проверяем максимальный лимит
        if (amount > MAX_AMOUNT) {
            sendMessage(chatId, String.format("❌ Ошибка: Сумма не может превышать %.0f рублей!\n\nПожалуйста, введите сумму меньше:",
                    MAX_AMOUNT));
            return;
        }

        // Проверяем количество знаков после запятой
        if (hasMoreThanTwoDecimalPlaces(amount)) {
            sendMessage(chatId, "❌ Ошибка: Не более 2 знаков после запятой!\n\nПожалуйста, введите сумму (например: 150.50):");
            return;
        }

        // Сохраняем сумму и переходим к следующему шагу
        dialog.setAmount(amount);
        dialog.setCurrentStep(ExpenseDialogState.DialogStep.WAITING_FOR_CATEGORY);

        // Показываем выбор категории
        showCategorySelection(chatId);
    }

    private void showCategorySelection(Long chatId) {
        StringBuilder message = new StringBuilder();
        List<String> categories = chatCategories.get(chatId);
        boolean empty = categories.isEmpty();

        message.append("📝 *Выберите или введите цель расходов:*\n\n");

        if (!empty) {
            message.append("*Существующие категории:*\n");

            for (int i = 0; i < categories.size(); i++) {
                message.append(String.format("%d. %s\n", i + 1, categories.get(i)));
            }
        }

        message.append("\n💡 *Вы можете:*\n");
        if (!empty) {
            message.append("• Ввести номер категории\n");
            message.append("• Ввести название существующей категории\n");
        }

        message.append("• Ввести название новой категории");

        sendMessage(chatId, message.toString());
    }

    private void handleCategoryInput(Long chatId, ExpenseDialogState dialog, String input) {
        String category;
        List<String> categories = chatCategories.get(chatId);

        // Проверяем, является ли ввод номером категории
        if (isNumeric(input)) {
            try {
                int index = Integer.parseInt(input) - 1;
                if (index >= 0 && index < categories.size()) {
                    category = categories.get(index);
                } else {
                    sendMessage(chatId, String.format("❌ Неверный номер! Введите число от 1 до %d:", categories.size()));
                    return;
                }
            } catch (NumberFormatException e) {
                sendMessage(chatId, "❌ Неверный формат! Введите номер категории или название:");
                return;
            }
        } else {
            // Используем введенный текст как категорию
            category = input.trim();

            // Если категории нет в списке - добавляем
            if (!categories.contains(category)) {
                categoryRepository.save(Category.builder().moneyChatId(chatId).name(category).build());
                chatCategories.get(chatId).add(category);
                sendMessage(chatId, String.format("✨ Новая категория '%s' добавлена в список!", category));
            }
        }

        dialog.setCategory(category);

        // Сохраняем расход (здесь будет логика сохранения в БД)
        saveExpense(dialog);

        // Завершаем диалог
        chatStates.remove(chatId);

        String amount = formatMoney(BigDecimal.valueOf(dialog.getAmount()));

        // Показываем результат
        String result = String.format(
                "✅ *Расход успешно добавлен!*\n\n" +
                        "💰 Сумма: %s руб.\n" +
                        "📂 Категория: %s\n\n" +
                        "Выберите действие в главном меню:",
                amount, category
        );

        // Отправляем результат отдельным сообщением, так как меню уже показано
        sendMessage(chatId, result);
        showMainMenu(chatId);
    }

    private void saveExpense(ExpenseDialogState state) {
        Expense expense = Expense.builder()
                .moneyChatId(state.getChatId())
                .amount(BigDecimal.valueOf(state.getAmount()))
                .category(state.getCategory())
                .expenseDate(LocalDateTime.now())
                .telegramId(state.getUser().getLeft())
                .username(state.getUser().getRight())
                .build();

        expenseRepository.save(expense);
    }

    private boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean hasMoreThanTwoDecimalPlaces(double value) {
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.stripTrailingZeros();
        int scale = bd.scale();
        return scale > MAX_DECIMAL_PLACES;
    }

    private void showMainMenu(Long chatId) {
        String menuText = "🏠 *Меню*\n\nЧто Вы хотите сделать?";
        sendMessage(chatId, menuText, markupBuilder.mainMenu());
    }

    private void sendMessage(Long chatId, String message) {
        sendMessage(chatId, message, null);
    }

    private void sendMessage(Long chatId, String message, InlineKeyboardMarkup keyboardMarkup) {
        if (StringUtils.isBlank(message)) {
            return;
        }

        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(message);
        Optional.ofNullable(keyboardMarkup).ifPresent(sendMessage::setReplyMarkup);

        try {
            telegramBot.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Can't send telegram message", e);
        }
    }

    private void editMessageText(Long chatId, Integer messageId, String text, InlineKeyboardMarkup keyboard) {
        if (StringUtils.isBlank(text)) {
            return;
        }

        org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText editMessage =
                new org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText();
        editMessage.enableMarkdown(true);
        editMessage.setChatId(String.valueOf(chatId));
        editMessage.setMessageId(messageId);
        editMessage.setText(text);

        try {
            telegramBot.execute(editMessage);
        } catch (TelegramApiException e) {
            log.error("Can't edit message", e);
        }

        if (keyboard != null) {
            org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup editMarkup =
                    new org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup();
            editMarkup.setChatId(String.valueOf(chatId));
            editMarkup.setMessageId(messageId);
            editMarkup.setReplyMarkup(keyboard);

            try {
                telegramBot.execute(editMarkup);
            } catch (TelegramApiException e) {
                log.error("Can't edit keyboard", e);
            }
        }
    }

    private void deleteMessage(Long chatId, Integer messageId) {
        org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage deleteMessage =
                new org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage();
        deleteMessage.setChatId(String.valueOf(chatId));
        deleteMessage.setMessageId(messageId);

        try {
            telegramBot.execute(deleteMessage);
        } catch (TelegramApiException e) {
            log.error("Can't delete message", e);
        }
    }

    private String formatMoney(BigDecimal amount) {
        return amount.setScale(0, RoundingMode.HALF_UP).toString();
    }

}