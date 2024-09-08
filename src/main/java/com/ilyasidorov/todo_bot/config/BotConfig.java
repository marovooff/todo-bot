package com.ilyasidorov.todo_bot.config;

import com.ilyasidorov.todo_bot.controller.BotController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.ArrayList;
import java.util.List;

/**
 * Регистрация бота.
 */
@Configuration
@Slf4j
public class BotConfig {

    @Bean
    public TelegramBotsApi telegramBotsApi(BotController bot) throws TelegramApiException {
        log.info("Initializing TelegramBotsApi");
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(bot);
        log.info("Bot registered successfully");
        setBotCommands(bot);
        return botsApi;
    }

    private void setBotCommands(BotController bot) {
        List<BotCommand> commands = new ArrayList<>();
        commands.add(new BotCommand("/add", "Добавить задачу"));
        commands.add(new BotCommand("/tags", "Список доступных тэгов"));
        commands.add(new BotCommand("/list_by_tag", "Список задач по тэгу"));
        commands.add(new BotCommand("/list", "Весь список задач"));
        commands.add(new BotCommand("/done", "Выполнить задачу"));

        try {
            bot.execute(new SetMyCommands(commands, new BotCommandScopeDefault(), null));
            log.info("Commands set successfully");
        } catch (TelegramApiException e) {
            log.error("Failed to set bot commands", e);
        }
    }
}
