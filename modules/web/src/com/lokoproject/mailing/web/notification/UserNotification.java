package com.lokoproject.mailing.web.notification;

import com.haulmont.cuba.gui.components.AbstractWindow;
import com.haulmont.cuba.security.global.UserSession;
import com.lokoproject.mailing.service.NotificationService;

import javax.inject.Inject;
import java.util.Map;

public class UserNotification extends AbstractWindow {

    @Inject
    private NotificationService notificationService;

    @Inject
    private UserSession userSession;

    @Override
    public void init(Map<String,Object> params){

    }
}