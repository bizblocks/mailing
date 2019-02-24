package com.lokoproject.mailing.web.mailing;

import com.haulmont.bali.util.ParamsMap;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.TimeSource;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.BaseAction;
import com.haulmont.cuba.gui.xml.DeclarativeAction;
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory;
import com.lokoproject.mailing.entity.GroovyScript;
import com.lokoproject.mailing.entity.Mailing;
import com.lokoproject.mailing.service.NotificationService;
import com.lokoproject.mailing.web.screens.Consolidation;


import javax.inject.Inject;
import java.util.*;

@SuppressWarnings("unchecked")
public class MailingBrowse extends EntityCombinedScreen {

    @Inject
    private FieldGroup fieldGroup;

    @Inject
    private ComponentsFactory componentsFactory;
    
    @Inject
    private Metadata metadata;

    @Inject
    private TimeSource timeSource;

    @Inject
    private NotificationService notificationService;

    @Override
    public void init(Map<String,Object> params){
        super.init(params);

        DeclarativeAction saveAction= (DeclarativeAction) getAction("save");
        removeAction(saveAction);

        BaseAction saveWithUpdateAction=new BaseAction("save"){
            @Override
            public void actionPerform(Component component){
                assert saveAction != null;
                saveAction.actionPerform(component);
                notificationService.updateMailing((Mailing) getTable().getDatasource().getItem());
            }
        };
        addAction(saveWithUpdateAction);

        addConditionSwitcherFieldForProperty("consolidation");

        initMailingPerformersField();

    }

    private void initMailingPerformersField(){
        fieldGroup.addField(fieldGroup.createField("mailingPerformers"));
        FieldGroup.FieldConfig fieldConfig=fieldGroup.getField("mailingPerformers");
        assert fieldConfig != null;
        fieldConfig.setCaption(getMessage("mailingPerformers"));

        PopupView popupView=componentsFactory.createComponent(PopupView.class);


        OptionsGroup optionsGroup=componentsFactory.createComponent(OptionsGroup.class);
        optionsGroup.setOptionsList(Arrays.asList("CubaWebClient","CubaEmail"));
        optionsGroup.setMultiSelect(true);
        optionsGroup.addValueChangeListener(event->{
            Collection<String> selected= (Collection<String>) event.getValue();
            if(selected==null) return;
            StringBuilder sb=new StringBuilder();
            int i=1;
            for(String s:selected){
                if("".equals(s)) continue;
                sb.append(s);
                if(i!=selected.size())sb.append(";");
                i++;
            }

            getSelectedMailing().setMailingPerformers( sb.toString());
            popupView.setMinimizedValue((getSelectedMailing().getMailingPerformers()==null)||("".equals(getSelectedMailing().getMailingPerformers()))?
                    getMessage("no_performers"):getSelectedMailing().getMailingPerformers());
        });

        popupView.setPopupContent(optionsGroup);
        popupView.setMinimizedValue(getMessage("no_performers"));
        popupView.setEnabled(false);
        fieldConfig.setComponent(popupView);
        fieldConfig.setWidth("400px");

        fieldGroup.getDatasource().addItemChangeListener(event->{
            Mailing mailing=getSelectedMailing();
            if(mailing!=null){
                popupView.setMinimizedValue((mailing.getMailingPerformers()==null)||("".equals(mailing.getMailingPerformers()))?
                    getMessage("no_performers"):mailing.getMailingPerformers());
                if(mailing.getMailingPerformers()!=null){
                    optionsGroup.setValue(Arrays.asList(mailing.getMailingPerformers().split(";")));
                }
                else {
                    optionsGroup.setValue(Collections.emptyList());
                }
            }
        });

        fieldGroup.addEditableChangeListener(event->{
            popupView.setEnabled(event.getSource().isEditable());
        });
    }

    private void addConditionSwitcherFieldForProperty(String propName){
        String propDisplayedName=String.format("%s %s",getMessage(propName),getMessage("condition"));
        fieldGroup.addField(fieldGroup.createField(propDisplayedName));

        FieldGroup.FieldConfig fieldConfig=fieldGroup.getField(propDisplayedName);
        fieldConfig.setCaption(propDisplayedName);

        HBoxLayout hBoxLayout=componentsFactory.createComponent(HBoxLayout.class);
        LookupField lookupField=componentsFactory.createComponent(LookupField.class);
        Button conditionButton=componentsFactory.createComponent(Button.class);
        PickerField consolidationConditionGroovyPicker=componentsFactory.createComponent(PickerField.class);
        
        consolidationConditionGroovyPicker.setMetaClass(metadata.getClass(GroovyScript.class));
        consolidationConditionGroovyPicker.addLookupAction();
        consolidationConditionGroovyPicker.addClearAction();

        lookupField.setOptionsList(Arrays.asList(
                getMessage("use_condition"),
                getMessage("use_groovy")
        ));
        lookupField.setNullOptionVisible(false);

        lookupField.addValueChangeListener(event->{
            if(event.getValue()==null) return;
            hBoxLayout.removeAll();
            hBoxLayout.add(lookupField);
            if(event.getValue().equals(getMessage("use_condition"))){
                hBoxLayout.add(conditionButton);
                hBoxLayout.expand(conditionButton);
            }
            else if(event.getValue().equals(getMessage("use_groovy"))){
                hBoxLayout.add(consolidationConditionGroovyPicker);
                hBoxLayout.expand(consolidationConditionGroovyPicker);
            }
        });

        hBoxLayout.add(lookupField);
        
        consolidationConditionGroovyPicker.addValueChangeListener(event->{
            getSelectedMailing().setValue(String.format("%s%s",propName,"Groovy"),event.getValue());
            getSelectedMailing().setValue(String.format("%s%s",propName,"Condition"),null);
        });
        
        conditionButton.setAction(new BaseAction("setup_condition"){
            @Override
            public void actionPerform(Component component){
                Consolidation consolidation= (Consolidation) openWindow("consolidation", WindowManager.OpenType.DIALOG, 
                        ParamsMap.of("condition",getSelectedMailing().getValue(String.format("%s%s",propName,"Condition"))));
                consolidation.addCloseListener(event->{
                    if("ok".equals(event)) {
                        getSelectedMailing().setValue(String.format("%s%s", propName, "Groovy"), null);
                        getSelectedMailing().setValue(String.format("%s%s", propName, "Condition"), consolidation.getCondition());
                        getSelectedMailing().setUpdateTs(timeSource.currentTimestamp()); //нужно было как-то добавить изменения, ведь consolidationCondition - транзиентное
                    }
                });
            }
        });
        
        
        hBoxLayout.setWidth("100%");
        hBoxLayout.setEnabled(false);

        conditionButton.setCaption(getMessage("setup_conditions"));

        fieldConfig.setWidth("400px");
        fieldConfig.setComponent(hBoxLayout);

        fieldGroup.addEditableChangeListener(event->{
            hBoxLayout.setEnabled(event.getSource().isEditable());
        });

        fieldGroup.getDatasource().addItemChangeListener(event->{
            Mailing selectedMailing= (Mailing) event.getItem();
            if(selectedMailing!=null){
                hBoxLayout.removeAll();
                hBoxLayout.add(lookupField);
                if(selectedMailing.getValue(String.format("%s%s",propName,"Groovy"))!=null){
                    lookupField.setValue(getMessage("use_groovy"));
                    hBoxLayout.add(consolidationConditionGroovyPicker);
                    hBoxLayout.expand(consolidationConditionGroovyPicker);
                    consolidationConditionGroovyPicker.setValue(selectedMailing.getValue(String.format("%s%s",propName,"Groovy")));
                }
                else if(selectedMailing.getValue(String.format("%s%s",propName,"Condition"))!=null){
                    lookupField.setValue(getMessage("use_condition"));
                    hBoxLayout.add(conditionButton);
                    hBoxLayout.expand(conditionButton);
                }
            }
        });
    }
    
    private Mailing getSelectedMailing(){
        return (Mailing) fieldGroup.getDatasource().getItem();
    }
}