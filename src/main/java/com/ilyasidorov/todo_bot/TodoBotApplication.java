package com.ilyasidorov.todo_bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TodoBotApplication {
	public static void main(String[] args) {
		SpringApplication.run(TodoBotApplication.class, args);
	}
}
