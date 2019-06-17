package com.lokoproject.mailing.web.notification;

import com.haulmont.bali.util.ParamsMap;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory;
import com.haulmont.cuba.security.global.UserSession;
import com.lokoproject.mailing.entity.Notification;
import com.lokoproject.mailing.entity.NotificationStage;
import com.lokoproject.mailing.service.NotificationService;
import com.lokoproject.mailing.web.beens.notification.CubaWebClientNotificationPerformer;
import com.lokoproject.mailing.web.beens.ui.UiAccessorCollector;

import javax.inject.Inject;
import java.util.Map;
import java.util.UUID;

public class UserNotification extends AbstractWindow {

    @Inject
    private NotificationService notificationService;

    @Inject
    private UserSession userSession;

    @Inject
    private TabSheet tabSheet;

    @Inject
    private Frame frame;

    @Inject
    private CollectionDatasource<Notification,UUID> tableNotificationsDs;

    @Inject
    private CollectionDatasource<Notification,UUID> notificationsDs;

    @Inject
    private GroupTable<Notification> notificationTable;

    @Inject
    private ComponentsFactory componentsFactory;

    @Inject
    private UiAccessorCollector uiAccessorCollector;

    @Inject
    CubaWebClientNotificationPerformer webClientNotificationPerformer;

    @Inject
    private VBoxLayout userMailingsVbox;

    @Override
    public void init(Map<String,Object> params){

        uiAccessorCollector.addAccessor(this,"userNotification",userSession.getUser());

        userMailingsVbox.setMargin(true,false,false,false);

        Component personalMailingBrowse=openFrame(null,"mailing$Mailing.browse",ParamsMap.of("idForPersonalSettings",userSession.getUser().getId()));
        personalMailingBrowse.setHeight("100%");
        userMailingsVbox.add(personalMailingBrowse);

        tableNotificationsDs.addItemChangeListener(event->{
            frame.removeAll();
            if(event.getItem()==null){
                return;
            }
            if((event.getPrevItem()!=null)&&(event.getPrevItem().equals(event.getItem()))){
               return;
            }
            webClientNotificationPerformer.markNotificationAsRead(event.getItem());
            frame.add(openFrame(frame,"notificationTemplateProcessor", ParamsMap.of("notificationTemplate",event.getItem().getTemplate() )));
        });



        tableNotificationsDs.addItemChangeListener(event->{
            if(event.getItem()==null){
                frame.removeAll();
                return;
            }
            if(event.getItem().equals(event.getPrevItem()))
            frame.removeAll();

            if((event.getPrevItem()!=null)&&(event.getPrevItem().equals(event.getItem()))){
                return;
            }
            if(event.getItem().getStage().getId()<NotificationStage.READ.getId()){
                notificationService.updateNotificationStage(event.getItem(),NotificationStage.READ);
                webClientNotificationPerformer.markNotificationAsRead(event.getItem());
            }

            frame.add(openFrame(frame,"notificationTemplateProcessor", ParamsMap.of("notificationTemplate",event.getItem().getTemplate() )));
        });

        notificationTable.addGeneratedColumn("theme",entity -> {
            if(entity.getTemplate()==null) return null;
            Label label=componentsFactory.createComponent(Label.class);
            label.setValue(entity.getTemplate().getTheme());
            return label;
        });

        notificationTable.getColumn("theme").setWidth(200);

        notificationTable.addStyleProvider((entity, property) -> {
            if(entity.getStage().getId()<NotificationStage.READ.getId()) return "bold";
            return null;
        });

        //notificationTable.sort("sendDateWOTime", Table.SortDirection.DESCENDING);
    }

    @Override
    public void ready(){
        notificationsDs.refresh();
        //два датасорса чтобы делать рефреш без хождения в базу
        notificationsDs.getItems().forEach(notification -> {
            tableNotificationsDs.addItem(notification);
        });

        Table.Column stageColumn=notificationTable.getColumn("stage");
        notificationTable.sort(stageColumn.toString(), Table.SortDirection.ASCENDING);
        stageColumn.setCollapsed(true);
    }

    public void updateNotificationStageInTable(Notification notification){
        if(notification==null) return;
        if(!tableNotificationsDs.containsItem(notification.getId())){
            if(NotificationStage.REMOVED.equals(notification.getStage())) return;
            tableNotificationsDs.addItem(notification);

        }
        Notification notificationToUpdate= tableNotificationsDs.getItem(notification.getId());
        if(notificationToUpdate!=null){
            if(NotificationStage.REMOVED.equals(notification.getStage())) {
                tableNotificationsDs.removeItem(notificationToUpdate);
            }
            else {
                notificationToUpdate.setStage(notification.getStage());
            }
        }
        notificationTable.repaint();
        
    }
}