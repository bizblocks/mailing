package com.lokoproject.mailing.web.mailing;

import com.haulmont.bali.util.ParamsMap;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.BaseAction;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.lokoproject.mailing.entity.Mailing;
import com.lokoproject.mailing.entity.Notification;
import com.lokoproject.mailing.service.NotificationService;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public class Mailingmanager extends AbstractWindow {

    @Inject
    private Table<Mailing> mailingsTable;

    @Inject
    private GroupTable<Notification> notificationsTable;

    @Inject
    private CollectionDatasource<Mailing,UUID> mailingsDs;

    @Inject
    private CollectionDatasource<Notification,UUID> notificationsDs;

    @Inject
    private Button activate;

    @Inject
    private Button deactivate;

    @Inject
    private NotificationService notificationService;

    @Override
    public void init(Map<String,Object> params){

        Action showNotificationAction=new BaseAction("show_notification"){
            @Override
            public void actionPerform(Component component){
                Notification notification=notificationsTable.getSingleSelected();
                if(notification==null) return;
                openWindow("notificationTemplateProcessor"
                        , WindowManager.OpenType.DIALOG
                        , ParamsMap.of("notificationTemplate",notification.getTemplate() ));
            }
        };

        notificationsTable.setItemClickAction(showNotificationAction);

        mailingsDs.addItemChangeListener(event->{
            if(event.getItem()==null) return;
            if(event.getItem().getActivated()==null) return;

            activate.setEnabled(!event.getItem().getActivated());
            deactivate.setEnabled(event.getItem().getActivated());
        });

        activate.setEnabled(false);
        deactivate.setEnabled(false);
    }

    public void onActivateClick() {
        setActivated(true);
    }

    public void onDeactivateClick() {
        setActivated(false);
    }

    public void onSendAgainClick() {
        sendNotificationAgain(false);
    }

    public void onUpdateMailingsClick() {
        Mailing mailing=mailingsTable.getSingleSelected();
        notificationService.updateMailing(mailing);
    }

    private void setActivated(Boolean value){
        Mailing mailing=mailingsTable.getSingleSelected();
        if(mailing==null) return;;
        mailing.setActivated(value);
        mailingsDs.commit();
        notificationService.updateMailing(mailing);
    }

    private void sendNotificationAgain(Boolean consolidate){
        Collection<Notification> notifications=notificationsTable.getSelected();
        notifications.forEach(notification -> {
            notificationService.sendNotificationAgain(notification, consolidate);
        });
    }
}