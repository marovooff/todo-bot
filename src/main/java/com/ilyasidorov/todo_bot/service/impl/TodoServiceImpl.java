package com.ilyasidorov.todo_bot.service.impl;

import com.ilyasidorov.todo_bot.model.entity.TodoStatus;
import com.ilyasidorov.todo_bot.repository.TodoRepository;
import com.ilyasidorov.todo_bot.model.entity.Todo;
import com.ilyasidorov.todo_bot.service.TodoService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TodoServiceImpl implements TodoService {

    /** Объект для работы с БД */
    private final TodoRepository repo;

    public TodoServiceImpl(TodoRepository repo) {
        this.repo = repo;
    }

    public List<Todo> getAllTodos() {
        return repo.findAll();
    }

    @Override
    public List<String> getUniqueTags() {
        return repo.findUniqueTags();
    }

    @Override
    public List<Todo> getTodosByTag(String tag) {
        return repo.findByTag(tag);
    }

    @Override
    public List<Todo> getTodosByStatus(TodoStatus status) {
        return repo.findByStatus(status);
    }

    @Override
    public List<Todo> getTodosByTagAndStatus(String tag, TodoStatus status) {
        return repo.findByTagAndStatus(tag, status);
    }

    @Override
    public void markTodoAsDone(Long todoId) {
        Todo todo = repo.findById(todoId).orElseThrow(() -> new RuntimeException("Task not found"));
        todo.setStatus(TodoStatus.DONE);
        repo.save(todo);
    }

    @Override
    public Todo addTodo(Todo todo) {
        return repo.save(todo);
    }
}
