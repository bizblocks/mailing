package com.lokoproject.mailing.notification.event;

import com.lokoproject.mailing.entity.Notification;
import org.springframework.context.ApplicationEvent;

/**
 * @author Antonlomako. created on 15.12.2018.
 */
public abstract class AbstractNotificationEvent extends ApplicationEvent {

    public abstract String getDefaultIdentifierName();

    private Notification notification;

    private Boolean isDeleteEvent=false;

    public AbstractNotificationEvent(Object source) {
        super(source);
    }


    public Notification getNotification() {
        return notification;
    }

    public void setNotification(Notification notification) {
        this.notification = notification;
    }

    public Boolean getDeleteEvent() {
        return isDeleteEvent;
    }

    public void setDeleteEvent(Boolean deleteEvent) {
        isDeleteEvent = deleteEvent;
    }
}
