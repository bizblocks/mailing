package com.lokoproject.mailing.service;


import com.haulmont.cuba.security.entity.User;
import com.lokoproject.mailing.entity.Mailing;
import com.lokoproject.mailing.entity.Notification;
import com.lokoproject.mailing.entity.NotificationStage;

import java.util.Collection;
import java.util.List;

public interface NotificationService {
    String NAME = "mailing_NotificationService";

    void updateMailing(Mailing mailing);

    void addNotification(Object object);

    void processMailings();

    Collection<Mailing> loadAllMailings();
    
    Notification updateNotificationStage(Notification notification, NotificationStage stage);

    void confirmNotificationReceipt(String notificationId);

    Notification getNotificationById(String id);

    List<Notification> getActualUserNotifications(User user, String notificationAgent);
}