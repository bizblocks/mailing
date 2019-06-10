package com.lokoproject.mailing.core.telegram.bot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * @author Antonlomako. created on 06.02.2019.
 */
public interface BotBehavior {
    void onUpdateReceived(SendMessage sendMessage, Update update);
    long getChatId();
}
