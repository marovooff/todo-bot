package com.ilyasidorov.todo_bot.service;

import com.ilyasidorov.todo_bot.model.entity.Todo;
import com.ilyasidorov.todo_bot.model.entity.TodoStatus;

import java.util.List;

public interface TodoService {

    /**
     * Получает список всех to-do.
     */
    public List<Todo> getAllTodos();

    /**
     * Получает список всех тэгов.
     */
    public List<String> getUniqueTags();

    /**
     * Получает список всех to-do, фильтрованных по тэгу
     */
    public List<Todo> getTodosByTag(String tag);

    /**
     * Получает список всех to-do, отфильтрованных по статусу
     */
    public List<Todo> getTodosByStatus(TodoStatus status);

    /**
     * Получает список всех to-do, отфильтрованных по тэгу и статусу
     */
    public List<Todo> getTodosByTagAndStatus(String tag, TodoStatus status);

    /**
     * Отмечает задачу выполненной
     */
    public void markTodoAsDone(Long todoId);

    /**
     * Добавляет to-do
     */
    public Todo addTodo(Todo todo);

}
