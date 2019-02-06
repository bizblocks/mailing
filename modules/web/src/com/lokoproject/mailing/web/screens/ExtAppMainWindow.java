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
import com.lokoproject.mailing.web.beens.ui.UiAccessorCollector;
import org.springframework.context.event.EventListener;

import javax.inject.Inject;
import java.util.Map;

public class ExtAppMainWindow extends AppMainWindow {

    @Inject
    private UiAccessorCollector uiAccessorCollector;

    @Inject
    private UserSession userSession;

    @Inject
    private BackgroundWorker backgroundWorker;

    @Inject
    private ComponentsFactory componentsFactory;

    @Override
    public void init(Map<String,Object> param){


        User currentUser = userSession.getUser();
        uiAccessorCollector.addAccessor(this,"main",currentUser);

        Frame frame=componentsFactory.createComponent(Frame.class);
        Window bell= (Window) openFrame(frame,"notificationBell");
        frame.add(bell);

        HBoxLayout hBoxLayout= (HBoxLayout) getComponent("titleBar");
        if(hBoxLayout!=null){
            hBoxLayout.add(frame,2);
        }
        frame.setWidth("50px");
        frame.setHeight("35px");
        uiAccessorCollector.addAccessor(bell,"notification",currentUser);
    }


    @EventListener
    protected void onApplicationEvent(CubaWebClientNotificationEvent event) {
        uiAccessorCollector.executeFor(event.getNotification().getTarget(),"main",window->{
            window.showNotification("it works");
        });
    }
}