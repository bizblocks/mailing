package com.lokoproject.mailing.notification.event;

/**
 * @author Antonlomako. created on 09.12.2018.
 */
public class TelegramNotificationEvent extends AbstractNotificationEvent {

    @Override
    public String getDefaultIdentifierName() {
        return "telegramId";
    }

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public TelegramNotificationEvent() {
        super("telegram_notification_event");
    }
}
