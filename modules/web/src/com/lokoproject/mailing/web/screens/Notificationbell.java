package com.lokoproject.mailing.web.screens;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.icons.Icons;
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory;
import com.haulmont.cuba.security.entity.User;
import com.lokoproject.mailing.service.NotificationService;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.JavaScriptFunction;

import javax.inject.Inject;
import java.util.Map;

public class Notificationbell extends AbstractWindow {

    @Inject
    private ComponentsFactory componentsFactory;

    @Inject
    private NotificationService notificationService;

    private int unreadNotificationValue;

    private Label label;
    private HBoxLayout hBoxLayout;

    @Override
    public void init(Map<String,Object> params){
        hBoxLayout=componentsFactory.createComponent(HBoxLayout.class);
        add(hBoxLayout);

        label=componentsFactory.createComponent(Label.class);
        label.setHtmlEnabled(true);
        label.setValue("<div class=\"container\">\n" +
                "    <div class=\"bell_notification\"></div>\n" +

                "</div>");

        hBoxLayout.add(label);

        JavaScript.getCurrent().addFunction("onBellClick", (JavaScriptFunction) arguments -> openWindow("userNotification", WindowManager.OpenType.NEW_TAB));
        JavaScript.getCurrent().execute("var el = document.querySelector('.bell_notification');\n" +
                "    el.onclick=function(){onBellClick();}");


    }

    public void increaseUnreadNotificationValue(){
        unreadNotificationValue++;
        updateUnreadNotificationValue();
        shakeBell();

    }

    private void updateUnreadNotificationValue() {
        JavaScript.getCurrent().execute("var el = document.querySelector('.bell_notification');\n" +
                "        var count = Number(el.getAttribute('data-count'));\n"+
                "    el.setAttribute('data-count', "+String.valueOf(unreadNotificationValue)+");\n" +
                "    el.classList.add('notify');\n" +
                "    if(count === 0){\n" +
                "        el.classList.add('show-count');\n" +
                "    }");
    }

    private void shakeBell() {
        JavaScript.getCurrent().execute("var el = document.querySelector('.bell_notification');\n" +
                "    el.classList.remove('notify');\n" +
                "    el.offsetWidth = el.offsetWidth;\n" +
                "    el.classList.add('notify');\n" +

                "    ");
    }

    public void resetUnreadNotificationValue(){
        unreadNotificationValue=0;
        updateUnreadNotificationValue();
    }

    public void setUnreadNotificationValue(Integer value){
        if(value<0) unreadNotificationValue=0;
        else unreadNotificationValue=value;
        updateUnreadNotificationValue();
    }


}