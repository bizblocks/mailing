package com.lokoproject.mailing.core.telegram.bot;

import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * @author Antonlomako. created on 06.02.2019.
 */
public interface BotBehavior {
    void onUpdateReceived(Update update);
}
