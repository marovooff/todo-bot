package com.ilyasidorov.todo_bot.controller;

import com.ilyasidorov.todo_bot.model.entity.Todo;
import com.ilyasidorov.todo_bot.model.entity.TodoStatus;
import com.ilyasidorov.todo_bot.service.TodoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@Slf4j
public class BotController extends TelegramLongPollingBot {

    @Value("${telegram.bot.username}")
    private String botUserName;

    @Value("${telegram.bot.token}")
    private String botToken;

    /**
     * Сервис взаимодействия с объектом to-do
     */
    @Autowired
    TodoService todoService;

    /**
     * Мапа для хранения состояний пользователя
     */
    private final Map<String, String> userStates = new HashMap<>();

    private final Map<String, Todo> tempTodoStorage = new HashMap<>();

    @Override
    public String getBotUsername() {
        return botUserName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        if (update.hasMessage() && message.hasText()) {
            String messageText = message.getText();
            String chatId = message.getChatId().toString();

            // Проверяем текущее состояние пользователя
            if (userStates.containsKey(chatId)) {
                handleUserState(chatId, messageText);
                return;
            }

            switch (messageText) {
                case "/add" -> handleAddCommand(chatId);
                case "/tags" -> handleTagCommand(chatId);
                case "/list_by_tag" -> handleListByTagCommand(chatId);
                case "/list" -> handleListCommand(chatId);
                case "/done" -> handleDoneCommand(chatId);
                default -> sendResponse(chatId, "Команда не распознана");
            }
        }
    }

    private void handleDoneCommand(String chatId) {
        List<Todo> todos = todoService.getTodosByStatus(TodoStatus.IN_PROGRESS);
        if (todos.isEmpty()) {
            sendResponse(chatId, "Нет задач в статусе IN_PROGRESS.");
            return;
        }
        String responseMessage = todos.stream()
                .map(todo -> todo.getId() + ": " + todo.getName())
                .collect(Collectors.joining("\n", "Задачи в статусе IN_PROGRESS:\n",
                        "\nВведите ID задачи, чтобы отметить ее как выполненную."));

        userStates.put(chatId, "AWAITING_DONE_TASK_ID");
        sendResponse(chatId, responseMessage);
    }

    private void handleListCommand(String chatId) {
        List<Todo> todos = todoService.getAllTodos();
        List<String> todoNames = todos.stream().map(Todo::getName).toList();
        String responseMessage = todoNames.stream().collect(Collectors.joining("\n", "Список задач:\n", ""));
        sendResponse(chatId, responseMessage);
    }

    private void handleListByTagCommand(String chatId) {
        sendResponse(chatId, "Введите тег для фильтрации задач:");
        userStates.put(chatId, "AWAITING_TAG_FOR_LIST");
    }

    private void handleTagCommand(String chatId) {
        List<String> tags = todoService.getUniqueTags();
        StringBuilder responseMessage = new StringBuilder("Доступные теги:\n");
        for (String tag : tags) {
            responseMessage.append("- ").append(tag).append("\n");
        }
        sendResponse(chatId, responseMessage.toString());
    }

    private void handleAddCommand(String chatId) {
        // Создаем новый объект Todo и сохраняем его временно
        Todo todo = new Todo();
        todo.setStatus(TodoStatus.IN_PROGRESS);
        todo.setDate(LocalDate.now());
        tempTodoStorage.put(chatId, todo);

        userStates.put(chatId, "AWAITING_NAME");
        sendResponse(chatId, "Введите название задачи:");
    }

    private void handleUserState(String chatId, String messageText) {
        String state = userStates.get(chatId);

        switch (state) {
            case "AWAITING_NAME":
                Todo todo = tempTodoStorage.get(chatId);
                todo.setName(messageText);

                userStates.put(chatId, "AWAITING_TAG");
                sendResponse(chatId, "Введите тег задачи:");
                break;

            case "AWAITING_TAG":
                todo = tempTodoStorage.get(chatId);
                todo.setTag(messageText);
                todoService.addTodo(todo);
                userStates.remove(chatId);
                tempTodoStorage.remove(chatId);

                sendResponse(chatId, "Задача добавлена!");
                break;
            case "AWAITING_TAG_FOR_LIST":
                List<Todo> todosByTag = todoService.getTodosByTagAndStatus(messageText, TodoStatus.IN_PROGRESS);
                if (todosByTag.isEmpty()) {
                    sendResponse(chatId, "Задач с таким тегом нет.");
                } else {
                    StringBuilder response = new StringBuilder("Задачи с тегом " + messageText + ":\n");
                    for (Todo todoItem : todosByTag) {
                        response.append("- ").append(todoItem.getName()).append("\n");
                    }
                    sendResponse(chatId, response.toString());
                }
                userStates.remove(chatId);
                break;
            case "AWAITING_DONE_TASK_ID":
                try {
                    Long taskId = Long.parseLong(messageText);
                    todoService.markTodoAsDone(taskId);
                    sendResponse(chatId, "Задача с ID " + taskId + " отмечена как выполненная.");
                } catch (NumberFormatException e) {
                    sendResponse(chatId, "Неверный формат ID. Попробуйте снова.");
                } catch (Exception e) {
                    sendResponse(chatId, "Ошибка: не удалось найти задачу с таким ID.");
                }
                userStates.remove(chatId);
                break;
            default:
                sendResponse(chatId, "Ошибка: Неизвестное состояние. Попробуйте снова.");
                userStates.remove(chatId);
                tempTodoStorage.remove(chatId);
                break;
        }
    }


    /**
     * Отправляет сообщение (ответ на введенную команду) в чат.
     *
     * @param chatId идентификатор чата
     * @param response текст сообщения
     */
    private void sendResponse (String chatId, String response) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(response);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
