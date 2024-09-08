package com.ilyasidorov.todo_bot.repository;

import com.ilyasidorov.todo_bot.model.entity.Todo;
import com.ilyasidorov.todo_bot.model.entity.TodoStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {

    @Query("SELECT DISTINCT t.tag FROM Todo t")
    List<String> findUniqueTags();

    List<Todo> findByTag(String tag);

    List<Todo> findByTagAndStatus(String tag, TodoStatus todoStatus);

    List<Todo> findByStatus(TodoStatus todoStatus);
}
