package com.lokoproject.mailing.core.telegram.bot;

import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Messages;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * @author Antonlomako. created on 06.02.2019.
 */
public class NotificationBot implements BotBehavior {

    public NotificationBot(StandardEntity entity, long chatId) {
        this.entity = entity;
        this.setChatId(chatId);
    }

    private Messages messages= AppBeans.get(Messages.class);

    private StandardEntity entity;
    private long chatId;

    @Override
    public void onUpdateReceived(SendMessage sendMessage, Update update) {
        sendMessage.setText(messages.getMainMessage("telegram_notification_bot_greeting")+" "+entity.getInstanceName());
    }

    @Override
    public long getChatId() {
        return chatId;
    }

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }
}
