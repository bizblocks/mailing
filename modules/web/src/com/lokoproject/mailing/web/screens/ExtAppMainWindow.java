package com.lokoproject.mailing.web.screens;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.executors.BackgroundWorker;
import com.haulmont.cuba.gui.icons.Icons;
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.cuba.security.global.UserSession;
import com.haulmont.cuba.web.app.mainwindow.AppMainWindow;
import com.haulmont.cuba.web.gui.components.WebButton;
import com.lokoproject.mailing.notification.event.CubaWebClientNotificationEvent;
import com.lokoproject.mailing.web.beens.notification.CubaWebClientNotificationPerformer;
import com.lokoproject.mailing.web.beens.ui.UiAccessorCollector;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinServletRequest;
import org.springframework.context.event.EventListener;

import javax.inject.Inject;
import java.util.Map;

public class ExtAppMainWindow extends AppMainWindow {

    @Inject
    private CubaWebClientNotificationPerformer webClientNotificationPerformer;

    @Override
    public void init(Map<String,Object> param){

        webClientNotificationPerformer.initByMainWindow(this);
    }

}