package com.lokoproject.mailing.notification.event;

import com.lokoproject.mailing.entity.Notification;
import org.springframework.context.ApplicationEvent;

/**
 * @author Antonlomako. created on 15.12.2018.
 */
public abstract class AbstractNotificationEvent extends ApplicationEvent {


    private Notification notification;

    public AbstractNotificationEvent(Object source) {
        super(source);
    }


    public Notification getNotification() {
        return notification;
    }

    public void setNotification(Notification notification) {
        this.notification = notification;
    }
}
