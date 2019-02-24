package com.lokoproject.mailing.web.screens;

import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory;
import com.lokoproject.mailing.entity.Notification;
import com.lokoproject.mailing.service.NotificationService;
import com.lokoproject.mailing.web.beens.notification.CubaWebClientNotificationPerformer;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.JavaScriptFunction;

import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Notificationbell extends AbstractWindow {

    @Inject
    private ComponentsFactory componentsFactory;

    @Inject
    private NotificationService notificationService;

    @Inject
    CubaWebClientNotificationPerformer webClientNotificationPerformer;

    private Label label;
    private HBoxLayout hBoxLayout;
    private Map<String,Notification> notificationMap=new LinkedHashMap<>();
    private String mainWindowHash;

    @Override
    public void init(Map<String,Object> params){
        hBoxLayout=componentsFactory.createComponent(HBoxLayout.class);
        add(hBoxLayout);

        label=componentsFactory.createComponent(Label.class);
        label.setHtmlEnabled(true);
        label.setValue("<div class='container'>" +
                "<div class='bell_notification'>" +
                    "<span class='bell-popuptext' id='notification-popup'></span>" +
                "</div>" +
                ""+
                "</div>");

        hBoxLayout.add(label);

        JavaScript.getCurrent().addFunction("onBellClick", (JavaScriptFunction) arguments -> openWindow("userNotification", WindowManager.OpenType.NEW_TAB));
        JavaScript.getCurrent().execute("var el = document.querySelector('.bell_notification');\n" +
                "    el.onclick=function(){onBellClick();}");

        JavaScript.getCurrent().addFunction("onBellHover", (JavaScriptFunction) arguments -> {
            if(notificationMap.size()>0)showPopupWithUnreadNotifications();
        });

        JavaScript.getCurrent().addFunction("onBellHoverOut", (JavaScriptFunction) arguments -> {
            hidePopupWithUnreadNotifications();
        });

        JavaScript.getCurrent().addFunction("onBellNotificationClick", (JavaScriptFunction) arguments -> {
            webClientNotificationPerformer.onNotificationClick(notificationMap.get(arguments.getString(0)),mainWindowHash);
        });

        JavaScript.getCurrent().execute("var el = document.querySelector('.bell_notification'); " +
                "var isMouseOver=false; " +
                "el.onmouseover=function(){isMouseOver=true;onBellHover()};" +
                "el.onmouseout=function(){isMouseOver=false;setTimeout(function() {if(!isMouseOver) onBellHoverOut(); }, 1500);}");
    }

    public void showPopupWithUnreadNotifications(){
        JavaScript.getCurrent().execute("var popup = document.getElementById('notification-popup'); popup.classList.add('show');");
    }

    public void hidePopupWithUnreadNotifications(){
        JavaScript.getCurrent().execute("var popup = document.getElementById('notification-popup'); popup.classList.remove('show');");
    }


    private void updateUnreadNotificationValue() {
        JavaScript.getCurrent().execute("var el = document.querySelector('.bell_notification');\n" +
                "        var count = Number(el.getAttribute('data-count'));\n"+
                "    el.setAttribute('data-count', "+String.valueOf(notificationMap.size())+");\n" +
                ((notificationMap.size()>0)? "el.classList.add('show-count');":"el.classList.remove('show-count');"));

    }

    private void shakeBell() {
        JavaScript.getCurrent().execute("var el = document.querySelector('.bell_notification');\n" +
                "    el.classList.remove('notify');\n" +
                "    el.offsetWidth = el.offsetWidth;\n" +
                "    el.classList.add('notify');\n" +

                "    ");
    }

    private StringBuilder buildNotificationLineInPopup(StringBuilder builder,Notification notification){
        if(builder==null) builder=new StringBuilder();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm");
        if(builder.length()>0) builder.append("<hr>");

//        builder.append( "<div onclick=\"onBellNotificationClick('").append(notification.getId().toString()).append("')\" ><div class='notification-line '><div class='bold-text'>")
        builder.append( "<div class='notification-item' id='").append(notification.getId().toString()).append("' ><div class='notification-theme '>")
                .append(notification.getTemplate().getTheme())
                .append("</div><div class='notification-line'><span>")
                .append(dateFormat.format(notification.getSendDate()))
                .append("</span><span style='margin: 08px;'>&#183;</span><span>")
                .append(notification.getMailing().getName()).append("</span></div></div>");

        return builder;

    }

    public void resetUnreadNotificationValue(){
        notificationMap.clear();
        hidePopupWithUnreadNotifications();
        updateUnreadNotificationValue();
    }

    public void setUnreadNotifications(List<Notification> notifications){
        notificationMap.clear();
        notifications.forEach(item->{
            notificationMap.put(item.getId().toString(),item);
        });
        updateUnreadNotificationValue();
        updatePopupWithUnreadNotifications();
        if(notificationMap.size()==0) hidePopupWithUnreadNotifications();
    }


    public void addNotification(Notification notification) {
        notificationMap.put(notification.getId().toString(),notification);
        shakeBell();
        updateUnreadNotificationValue();
        updatePopupWithUnreadNotifications();
    }

    public void removeNotification(Notification notification){
        notificationMap.remove(notification.getId().toString());
        if(notificationMap.size()==0) hidePopupWithUnreadNotifications();
        updateUnreadNotificationValue();
        updatePopupWithUnreadNotifications();
    }

    private void updatePopupWithUnreadNotifications() {

        StringBuilder popupContentBuilder=new StringBuilder();

        notificationMap.forEach((id,notification)->{
            buildNotificationLineInPopup(popupContentBuilder,notification);
        });

        String result=popupContentBuilder.toString();

        JavaScript.getCurrent().execute("document.getElementById('notification-popup').innerHTML=\""+result+"\";");

        StringBuilder clickListenerBuilder=new StringBuilder();//popupContentBuilder;
        notificationMap.forEach((id,notification)->{
            clickListenerBuilder.append("document.getElementById('")
                    .append(id)
                    .append("').onclick=function(event){event.stopPropagation();console.warn('").append(id).append("');onBellNotificationClick('").append(id).append("')};");

        });

        JavaScript.getCurrent().execute(clickListenerBuilder.toString());

    }

    public String getMainWindowHash() {
        return mainWindowHash;
    }

    public void setMainWindowHash(String mainWindowHash) {
        this.mainWindowHash = mainWindowHash;
    }
}