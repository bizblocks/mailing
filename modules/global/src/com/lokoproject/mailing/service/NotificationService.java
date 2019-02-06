package com.lokoproject.mailing.service;


import com.lokoproject.mailing.entity.Mailing;
import com.lokoproject.mailing.entity.Notification;
import com.lokoproject.mailing.entity.NotificationStage;

import java.util.Collection;

public interface NotificationService {
    String NAME = "mailing_NotificationService";

    void addNotification(Object object);

    void processMailings();

    Collection<Mailing> loadAllMailings();

    Notification updateNotificationStage(Notification notification, NotificationStage stage);

    Collection<Notification> getNotifications(String ids);
}