package com.lokoproject.mailing.web.screens;

import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.View;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.DsBuilder;
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.cuba.security.global.UserSession;
import com.lokoproject.mailing.entity.Mailing;
import com.lokoproject.mailing.service.BotService;
import com.lokoproject.mailing.service.DaoService;
import com.lokoproject.mailing.service.IdentifierService;
import com.lokoproject.mailing.service.NotificationService;
import com.lokoproject.mailing.utils.HtmlTemplateHelper;
import com.lokoproject.mailing.utils.ReflectionHelper;
import com.lokoproject.mailing.web.beens.notification.CubaWebClientNotificationPerformer;
import com.lokoproject.mailing.web.beens.ui.UiAccessorCollector;

import javax.inject.Inject;
import java.io.IOException;
import java.util.*;

public class Screen extends AbstractWindow {

    @Inject
    private TextField textField;

    @Inject
    private TextField enumPackageField;

    @Inject
    private LookupField typeField;

    @Inject
    private LookupField enumTypeField;

    @Inject
    private LookupField enumValueField;

    @Inject
    private LookupPickerField entityField;

    @Inject
    private LookupField typeSwitchField;

    @Inject
    private TextField mapKeyField;

    @Inject
    private VBoxLayout resultVbox;

    @Inject
    private LookupField simpleTypeField;

    @Inject
    private HBoxLayout entityHbox;

    @Inject
    private HBoxLayout simpleTypeHbox;

    @Inject
    private FlowBoxLayout enumHbox;

    @Inject
    private LookupPickerField mailingField;


    @Inject
    private NotificationService notificationService;

    @Inject
    private TextField messageContentTextField;

    @Inject
    private TextField userNameTextField;

    @Inject
    private CubaWebClientNotificationPerformer notificationProcessor;

    @Inject
    private BotService botService;

    @Inject
    private TextArea htmlTextField;

    @Inject
    private DaoService daoService;

    @Inject
    private IdentifierService identifierService;

    @Inject
    private UiAccessorCollector uiAccessorCollector;

    @Inject
    private CubaWebClientNotificationPerformer notificationPerformer;

    @Inject
    private UserSession userSession;

    @Inject
    private Metadata metadata;

    @Inject
    private ComponentsFactory componentsFactory;


    private Object objectToAddToNotification;

    @Override
    public void init(Map<String,Object> params){
        initEntityField();
        initSimpleTypeField();
        initEnumField();
        initTypeSwitchField();
    }

    private void initTypeSwitchField() {
        typeSwitchField.setOptionsList(Arrays.asList("simple","entity","enum"));
        typeSwitchField.setNullOptionVisible(false);
        typeSwitchField.setValue("simple");
        entityHbox.setVisible(false);
        enumHbox.setVisible(false);
        typeSwitchField.addValueChangeListener(event->{
            if("simple".equals(event.getValue())){
                entityHbox.setVisible(false);
                enumHbox.setVisible(false);
                simpleTypeHbox.setVisible(true);
            }
            else if("entity".equals(event.getValue())){
                entityHbox.setVisible(true);
                enumHbox.setVisible(false);
                simpleTypeHbox.setVisible(false);
            }
            else{
                entityHbox.setVisible(false);
                enumHbox.setVisible(true);
                simpleTypeHbox.setVisible(false);
            }
        });
    }

    private void initSimpleTypeField() {
        simpleTypeField.setOptionsList(Arrays.asList("boolean","integer","double","string"));
        simpleTypeField.setNullOptionVisible(false);
        simpleTypeField.setValue("string");
    }

    private void initEntityField() {
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

    private Map<String,Class> enumMap=new HashMap<>();
    private void initEnumField(){
        enumPackageField.addValueChangeListener(event->{
            if(event.getValue()==null) return;
            List<String> metaclassList=new ArrayList<>();
            ReflectionHelper.getAllDeclaredEnum((String) event.getValue()).forEach(enumItem->{
                metaclassList.add(enumItem.getSimpleName());
                enumMap.put(enumItem.getSimpleName(),enumItem);
            });
            enumTypeField.setOptionsList(metaclassList);
        });

        enumTypeField.setNullOptionVisible(false);
        enumTypeField.addValueChangeListener(event->{
            Class enumItem=enumMap.get(event.getValue());
            if(enumItem==null) return;
            Object [] values=enumItem.getEnumConstants();
            enumValueField.setOptionsList(Arrays.asList(values));

            enumValueField.setEnabled(true);
        });

        entityField.setEnabled(false);
        enumValueField.setNullOptionVisible(false);
    }

    public void onShakeBellClick() {
        this.uiAccessorCollector.executeFor(this.userSession.getUser(), "notification", (bellWindow) -> {
            if(bellWindow instanceof Notificationbell) {
                Notificationbell bell = (Notificationbell)bellWindow;
                bell.shakeBell();
            }

        });
    }

    public void onShowDesctopNotificationClick() {
        this.notificationPerformer.showDesktopNotificationToUser(this.userSession.getUser().getLogin(), "hello", "hello content", "", "", (CubaWebClientNotificationPerformer.NotificationClickListener)null);
    }

    public void onStartBotClick() {
        this.botService.startBot();
    }

    public void onSendMsgClick() {
        User user = this.daoService.getUserByLogin(this.userNameTextField.getRawValue());
        this.botService.sendMessageToUser(user, this.messageContentTextField.getRawValue());
    }

    public void onSendWebMsgClick() {
        User user = this.daoService.getUserByLogin(this.userNameTextField.getRawValue());
        this.notificationService.sendSimpleNotification(user, "hello content", "header", "CubaWebClient");
    }

    public void onHtmlButtonClick() throws IOException {
        User user = this.daoService.getUserByLogin(this.userNameTextField.getRawValue());
        this.botService.sendImageToUser(user, HtmlTemplateHelper.createImageByHtml(this.htmlTextField.getRawValue(), 1024, 768).toByteArray(), "img test");
    }

    public void onAddToMapClick() {
        if(mapKeyField.getValue()==null){
            showNotification(getMessage("set_key"),NotificationType.WARNING);
            return;
        }
        Object object=getObjectToAdd();
        if(object==null){
            showNotification(getMessage("set_object"),NotificationType.WARNING);
            return;
        }
        if(!(objectToAddToNotification instanceof Map)){
            objectToAddToNotification=new HashMap<>();
        }
        Map map= (Map) objectToAddToNotification;
        map.put(mapKeyField.getRawValue(),object);
        updateResultVbox();
    }

    private void updateResultVbox() {
        resultVbox.removeAll();
        if(objectToAddToNotification==null) return;
        if(objectToAddToNotification instanceof Map){
            Map map= (Map) objectToAddToNotification;
            map.forEach((key,value)->{
                HBoxLayout hbox=componentsFactory.createComponent(HBoxLayout.class);
                Label keyLabel=componentsFactory.createComponent(Label.class);
                Label valueLabel=componentsFactory.createComponent(Label.class);
                Label spacer=componentsFactory.createComponent(Label.class);
                spacer.setValue("   -   ");
                hbox.add(keyLabel);
                hbox.add(spacer);
                hbox.add(valueLabel);
                hbox.setSpacing(true);
                keyLabel.setValue(key.toString());
                valueLabel.setValue(value.toString());
                resultVbox.add(hbox);
            });
        }
        else if(objectToAddToNotification instanceof List){
            List list= (List) objectToAddToNotification;
            list.forEach(item->{
                Label itemLabel=componentsFactory.createComponent(Label.class);
                itemLabel.setValue(item.toString());
                resultVbox.add(itemLabel);
            });
        }
        else{
            Label itemLabel=componentsFactory.createComponent(Label.class);
            itemLabel.setValue(objectToAddToNotification.toString());
            resultVbox.add(itemLabel);
        }
    }

    private Object getObjectToAdd() {
        if("simple".equals(typeSwitchField.getValue())){
            if("boolean".equals(simpleTypeField.getValue())){
                return Boolean.valueOf(textField.getRawValue());
            }
            else if("integer".equals(simpleTypeField.getValue())){
                return Integer.valueOf(textField.getRawValue());
            }
            else if("double".equals(simpleTypeField.getValue())){
                return Double.valueOf(textField.getRawValue());
            }
            else if("string".equals(simpleTypeField.getValue())){
                return textField.getRawValue();
            }
            else return null;
        }
        else if("entity".equals(typeSwitchField.getValue())){
            return entityField.getValue();
        }
        else{
            return enumValueField.getValue();
        }

    }

    public void onAddToListClick() {
        Object object=getObjectToAdd();
        if(object==null){
            showNotification(getMessage("set_object"),NotificationType.WARNING);
            return;
        }
        if(!(objectToAddToNotification instanceof List)){
            objectToAddToNotification=new ArrayList<>();
        }
        List list= (List) objectToAddToNotification;
        list.add(object);
        updateResultVbox();
    }

    public void onAddAsIsClick() {
        objectToAddToNotification=getObjectToAdd();
        updateResultVbox();
    }

    public void onAddNotificationClick() {
        if(objectToAddToNotification==null){
            showNotification(getMessage("set_object"),NotificationType.WARNING);
            return;
        }
        if(mailingField.getValue()==null){
            notificationService.addNotification(objectToAddToNotification);
        }
        else{
            Mailing mailing=mailingField.getValue();
            notificationService.addNotification(mailing,objectToAddToNotification);
        }
    }
}