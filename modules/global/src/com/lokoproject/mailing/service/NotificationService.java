package com.lokoproject.mailing.service;


import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.cuba.security.entity.User;
import com.lokoproject.mailing.dto.ResultOfAddingNotification;
import com.lokoproject.mailing.entity.Mailing;
import com.lokoproject.mailing.entity.Notification;
import com.lokoproject.mailing.entity.NotificationStage;
import com.lokoproject.mailing.notification.template.TemplateWrapper;

import java.util.Collection;
import java.util.List;

public interface NotificationService {
    String NAME = "mailing_NotificationService";

    void updateMailing(Mailing mailing);

    void sendSimpleNotification(StandardEntity target, String content, String header, String channelName);

    ResultOfAddingNotification addNotification(Object object);

    ResultOfAddingNotification addNotification(Mailing mailing, Object object);

    ResultOfAddingNotification addNotification(Object object, Boolean onlyCheck);

    ResultOfAddingNotification addNotification(Mailing mailing, Object object, Boolean onlyCheck);

    void processMailings();

    TemplateWrapper buildNotificationTemplate(Notification notification);
    
    Notification updateNotificationStage(Notification notification, NotificationStage stage);

    void confirmNotificationReceipt(String notificationId);

    Notification getNotificationById(String id);

    List<Notification> getActualUserNotifications(User user, String notificationAgent);

    void sendNotificationAgain(Notification notification,boolean consolidate);

    void onRemoveMailing(Mailing item);
}