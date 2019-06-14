package com.lokoproject.mailing.web.mailing;

import com.haulmont.bali.util.ParamsMap;
import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.View;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.BaseAction;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.DsBuilder;
import com.haulmont.cuba.gui.data.HierarchicalDatasource;
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory;
import com.lokoproject.mailing.dto.ChanelInfo;
import com.lokoproject.mailing.entity.JustTransient;
import com.lokoproject.mailing.entity.Mailing;
import com.lokoproject.mailing.entity.Notification;
import com.lokoproject.mailing.service.ChannelStateService;
import com.lokoproject.mailing.service.DaoService;
import com.lokoproject.mailing.service.MailingService;
import com.lokoproject.mailing.service.NotificationService;

import javax.inject.Inject;
import java.util.*;

public class Mailingmanager extends AbstractWindow {

    @Inject
    private ChannelStateService channelStateService;

    @Inject
    private Messages messages;

    @Inject
    private ComponentsFactory componentsFactory;

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

    @Inject
    private CollectionDatasource userSettingsMailingsDs;

    @Inject
    private VBoxLayout vboxForSelectedEntity;

    @Inject
    private SplitPanel userSettingsSplit;

    @Inject
    private CollectionDatasource chanelDs;

    @Inject
    private FieldGroup chanelFieldGroup;

    @Inject
    private DaoService daoService;

    @Inject
    private Metadata metadata;

    @Inject
    private HierarchicalDatasource<JustTransient,UUID> entitiesWithCustomSettingsDs;

    @Inject
    private LookupField typeField;

    @Inject
    private LookupPickerField entityField;

    @Inject
    private LookupPickerField mailingField;

    @Inject
    private MailingService mailingService;


    @Override
    public void init(Map<String,Object> params){
        initNotificationBrowseTab();
        initPersonalSettingsDs();
        initMailingUserSettingsTab();
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
        if(mailing==null){
            showNotification(getMessage("select_mailing"),NotificationType.WARNING);
            return;
        }
        notificationService.updateMailing(mailing);
    }

    private Map<String,JustTransient> typeMap=new HashMap<>();
    private Map<String,JustTransient> idMap=new HashMap<>();
    private Map<String,StandardEntity> entityIdMap=new HashMap<>();
    private Map<String,Set<Mailing>> mailingsOfType=new HashMap<>();
    private Map<String,Set<Mailing>> mailingsOfEntity=new HashMap<>();

    private void initPersonalSettingsDs(){
        List<Mailing> allPersonalizedMailings=daoService.getAllPersonalizedMailing();
        allPersonalizedMailings.forEach(mailing -> {
            
            if(mailing.getEntityIdForPersonalSettings()==null){
                mailingsOfType.putIfAbsent(mailing.getEntityTypeForPersonalSettings(),new HashSet<>());
                mailingsOfType.get(mailing.getEntityTypeForPersonalSettings()).add(mailing);

                JustTransient justTransient=metadata.create(JustTransient.class);
                justTransient.setName(mailing.getEntityTypeForPersonalSettings());
                typeMap.put(mailing.getEntityTypeForPersonalSettings(),justTransient);
            }
            else{
                mailingsOfEntity.putIfAbsent(mailing.getEntityIdForPersonalSettings().toString(),new HashSet<>());
                mailingsOfEntity.get(mailing.getEntityIdForPersonalSettings().toString()).add(mailing);

                StandardEntity entity=daoService.getEntity(mailing.getEntityTypeForPersonalSettings(),mailing.getEntityIdForPersonalSettings().toString());
                if(entity==null) return;
                entityIdMap.put(mailing.getEntityIdForPersonalSettings().toString(),entity);
                JustTransient justTransient=metadata.create(JustTransient.class);
                justTransient.setName(entity.getInstanceName());
                justTransient.setValueOne(entity.getId().toString());
                justTransient.setValueTwo(mailing.getEntityTypeForPersonalSettings());
                idMap.put(mailing.getEntityIdForPersonalSettings().toString(),justTransient);
            }

        });

        typeMap.values().forEach(item->{
            entitiesWithCustomSettingsDs.addItem(item);
        });

        idMap.values().forEach(idItem->{
            JustTransient parent=typeMap.get(idItem.getValueTwo());
            if(parent==null){
                parent=metadata.create(JustTransient.class);
                parent.setName(idItem.getValueTwo());
                typeMap.put(idItem.getValueTwo(),parent);
                entitiesWithCustomSettingsDs.addItem(parent);
            }
            idItem.setParent(parent);
            entitiesWithCustomSettingsDs.addItem(idItem);
        });

    }

    private void initNotificationBrowseTab(){
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

    private void initMailingUserSettingsTab(){
        entitiesWithCustomSettingsDs.addItemChangeListener(event->{
            updateMailingFrame(event.getItem());
        });

        List<String> metaclassList=new ArrayList<>();
        metadata.getSession().getClasses().forEach(item->{
            metaclassList.add(item.getName());
        });
        typeField.setOptionsList(metaclassList);
        typeField.setNullOptionVisible(false);
        typeField.addValueChangeListener(event->{
            if(event.getValue()==null) return;
            CollectionDatasource ds = new DsBuilder(getDsContext())
                    .setJavaClass(metadata.getClass((String)event.getValue()).getJavaClass())
                    .setViewName(View.LOCAL)
                    .setId("entityDs")
                    .buildCollectionDatasource();
            ds.refresh();
            entityField.setOptionsDatasource(ds);
            entityField.setEnabled(true);
        });

        entityField.setEnabled(false);

    }
    
    private void updateMailingFrame(JustTransient selectedItem){
        vboxForSelectedEntity.removeAll();
        if(selectedItem==null) return;

        Component component;
        if(selectedItem.getValueOne()!=null){
            component=openFrame(null,"mailing$Mailing.browse",ParamsMap.of("idForPersonalSettings",UUID.fromString(selectedItem.getValueOne()),
                    "typeForPersonalSettings",selectedItem.getValueTwo()));
            entityField.setValue(entityIdMap.get(selectedItem.getValueOne()));
            typeField.setValue(selectedItem.getValueTwo());
        }
        else{
            component=openFrame(null,"mailing$Mailing.browse",ParamsMap.of("typeForPersonalSettings",selectedItem.getName()));
            typeField.setValue(selectedItem.getName());
            entityField.setValue(null);
        }
        component.setHeight("100%");
        vboxForSelectedEntity.add(component);
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

    public void onAddPersonalizationBtnClick() {
        if(typeField.getValue()==null){
            showNotification(getMessage("select_type"),NotificationType.WARNING);
            return;
        }
        if(mailingField.getValue()==null){
            showNotification(getMessage("select_mailing"),NotificationType.WARNING);
            return;
        }

        UUID entityId=entityField.getValue()==null? null:((StandardEntity) entityField.getValue()).getId();
        StandardEntity entity=entityField.getValue()==null? null:((StandardEntity) entityField.getValue());
        String type=typeField.getValue();
        Mailing mailing=mailingField.getValue();

        Mailing personalizedMailing=mailingService.createPersonalSettings(entityId,type,mailing);

        JustTransient newItem;
        if(entityId!=null){
            newItem=idMap.get(entityId.toString());
            if(newItem==null){
                newItem=metadata.create(JustTransient.class);
                newItem.setName(entity.getInstanceName());
                newItem.setValueOne(entity.getId().toString());
                idMap.put(entityId.toString(),newItem);
            }

            mailingsOfEntity.putIfAbsent(personalizedMailing.getEntityIdForPersonalSettings().toString(),new HashSet<>());
            mailingsOfEntity.get(personalizedMailing.getEntityIdForPersonalSettings().toString()).add(personalizedMailing);
            entityIdMap.put(entityId.toString(),entity);

            JustTransient parent=typeMap.get(typeField.getValue());
            if(parent==null){
                parent=metadata.create(JustTransient.class);
                parent.setName(type);
                entitiesWithCustomSettingsDs.addItem(parent);
            }
            newItem.setParent(parent);

        }
        else{
            newItem=typeMap.get(type)  ;
            if(newItem==null) {
                newItem = metadata.create(JustTransient.class);
                newItem.setName(type);
                typeMap.put(type, newItem);
            }
            mailingsOfType.putIfAbsent(mailing.getEntityTypeForPersonalSettings(),new HashSet<>());
            mailingsOfType.get(mailing.getEntityTypeForPersonalSettings()).add(mailing);

        }

        entitiesWithCustomSettingsDs.addItem(newItem);
        updateMailingFrame(entitiesWithCustomSettingsDs.getItem());
    }
}