package com.lokoproject.mailing.web.screens;

import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.DsBuilder;
import com.haulmont.cuba.gui.data.HierarchicalDatasource;
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory;
import com.haulmont.cuba.web.gui.WebWindow;
import com.lokoproject.mailing.conditions.Condition;
import com.lokoproject.mailing.conditions.ConditionException;
import com.lokoproject.mailing.conditions.ConditionFactory;
import com.lokoproject.mailing.conditions.ConditionGroup;
import com.lokoproject.mailing.entity.JustTransient;
import com.lokoproject.mailing.utils.Serializer;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

@SuppressWarnings("unchecked")
public class Consolidation extends AbstractWindow {

    @Inject
    private Messages messages;

    @Inject
    private ComponentsFactory componentsFactory;

    @Inject
    private Tree<JustTransient> availableItemsTree;

    @Inject
    private Tree<JustTransient> selectedItemsTree;

    @Inject
    private HierarchicalDatasource<JustTransient,UUID> availableItemsDs;

    @Inject
    private HierarchicalDatasource<JustTransient,UUID> selectedItemsDs;

    @Inject
    private Button addBtn;

    @Inject
    private Button deleteBtn;

    @Inject
    private VBoxLayout settingsVbox;

    ConditionFactory conditionFactory=new ConditionFactory();

    private Map<JustTransient,Condition> resultTransientConditionMap=new HashMap<>();
    private Map<Condition,JustTransient> resultConditionTransientMap=new HashMap<>();
    private List<Condition> conditionsWithErrors=new ArrayList<>();


    @Override
    public void init(Map<String,Object> params){

        if(params.get("condition")!=null){
            try {
                Object serialized= Serializer.serializeObject((Serializable) params.get("condition"));
                Condition condition= (Condition) Serializer.fromString((String) serialized);
                createConditionPresentation(condition,null);
                selectedItemsTree.expandTree();
            } catch (Exception e) {
                showNotification(getMessage("condition_serialization_error"),NotificationType.ERROR);
                onAddOrClick();
            }
        }
        else{
            onAddOrClick();
        }

        addBtn.setEnabled(false);
        deleteBtn.setEnabled(false);

        conditionFactory.getAllExecutionCheckItemsNamesGroupedByType().forEach((group,classes)->{
            JustTransient groupObject=new JustTransient();
            groupObject.setName(group.substring(group.lastIndexOf(".")+1));
            availableItemsDs.addItem(groupObject);

            classes.forEach(classItem->{
                JustTransient classObject=new JustTransient();
                classObject.setName(classItem);
                classObject.setParent(groupObject);
                availableItemsDs.addItem(classObject);
            });
        });

        availableItemsDs.addItemChangeListener(event->{
            addBtn.setEnabled((event.getItem()!=null)
                    &&(event.getItem().getParent()!=null)
                    &&(resultTransientConditionMap.get(selectedItemsDs.getItem()) instanceof ConditionGroup));
        });

        selectedItemsDs.addItemChangeListener(event->{
            deleteBtn.setEnabled((event.getItem()!=null));

            addBtn.setEnabled((availableItemsDs.getItem()!=null)
                    &&(availableItemsDs.getItem().getParent()!=null)
                    &&(resultTransientConditionMap.get(event.getItem()) instanceof ConditionGroup));

            settingsVbox.removeAll();
            Component settings=createConditionSettingsComponent(resultTransientConditionMap.get(event.getItem()));
            if(settings!=null){
                settingsVbox.add(settings);
            }
        });

        availableItemsTree.expandTree();
        selectedItemsTree.addStyleProvider(entity->{
            Condition condition=resultTransientConditionMap.get(entity);
            if(conditionsWithErrors.contains(condition)) {
                return "red";
            }
            return null;
        });

    }

    public Condition getCondition(){
        JustTransient justTransient=selectedItemsDs.getItem(selectedItemsDs.getRootItemIds().iterator().next());
        return resultTransientConditionMap.get(justTransient);
    }

    public void onAddBtnClick() throws ConditionException {
        addCondition(conditionFactory.createCondition(availableItemsDs.getItem().getName()));
    }

    public void onDeleteBtnClick() {
        deleteCondition(selectedItemsDs.getItem());
    }

    public void onAddAndClick() {
        addCondition(conditionFactory.createAndCondition());
    }

    public void onAddOrClick() {
        addCondition(conditionFactory.createOrCondition());
    }

    private void addCondition(Condition condition){

        JustTransient newItem=new JustTransient();

        if(selectedItemsDs.getItem()==null){
            if(selectedItemsDs.getItems().size()>0) {
                UUID rootId=selectedItemsDs.getRootItemIds().iterator().next();
                selectedItemsDs.setItem(selectedItemsDs.getItem(rootId));
                addCondition(condition);
                return;
            }
            resultConditionTransientMap.put(condition,newItem);
            resultTransientConditionMap.put(newItem,condition);
        }
        else{
            JustTransient parent=selectedItemsDs.getItem();
            Condition parentCondition=resultTransientConditionMap.get(parent);

            newItem.setParent(parent);
            //condition.setParent(parentCondition);
            ConditionGroup conditionGroup= (ConditionGroup) parentCondition;
            conditionGroup.addChild(condition);

            resultConditionTransientMap.put(condition,newItem);
            resultTransientConditionMap.put(newItem,condition);
        }
        updateDisplayedValue(condition);
        selectedItemsDs.addItem(newItem);

        selectedItemsTree.expandTree();
    }


    private boolean validateConditionsFields(){
        conditionsWithErrors.clear();
        for(Condition condition:resultConditionTransientMap.keySet()) {
            if(!condition.validateFields()) conditionsWithErrors.add(condition);
        }
        return conditionsWithErrors.size()==0;
    }

    private void createConditionPresentation(Condition condition,JustTransient parent){

        JustTransient newItem=new JustTransient();

        newItem.setParent(parent);
        selectedItemsDs.addItem(newItem);

        resultConditionTransientMap.put(condition,newItem);
        resultTransientConditionMap.put(newItem,condition);

        updateDisplayedValue(condition);

        if(condition instanceof ConditionGroup){
            ConditionGroup conditionGroup= (ConditionGroup) condition;
            conditionGroup.getChildren().forEach(child->{
                createConditionPresentation(child,newItem);
            });
        }
    }

    private void deleteCondition(JustTransient justTransient){
        Condition conditionToDelete=resultTransientConditionMap.get(justTransient);
        Condition parent=resultTransientConditionMap.get(justTransient.getParent());
        if(parent!=null) {
            ConditionGroup conditionGroup= (ConditionGroup) parent;
            conditionGroup.getChildren().remove(conditionToDelete);
        }
        resultTransientConditionMap.remove(justTransient);
        resultConditionTransientMap.remove(conditionToDelete);

        removeTransientRecur(justTransient);
    }

    private void removeTransientRecur(JustTransient justTransient){
        if(justTransient==null) return;
        selectedItemsDs.getChildren(justTransient.getId()).forEach(childId->{
            removeTransientRecur(selectedItemsDs.getItem(childId));
        });
        selectedItemsDs.removeItem(justTransient);
    }

    private Component createConditionSettingsComponent(Condition condition){
        if(condition==null) return null;
        VBoxLayout vbox=componentsFactory.createComponent(VBoxLayout.class);
        Arrays.asList(condition.getClass().getDeclaredFields()).forEach(field -> {
            Condition.FieldDescription fieldDescription=field.getAnnotation(Condition.FieldDescription.class);
            if(fieldDescription!=null){

                field.setAccessible(true);

                String componentLabel;
                if("default".equals(fieldDescription.name())){
                    componentLabel=field.getName();
                }
                else{
                    componentLabel=fieldDescription.name();
                }

                HBoxLayout hBoxLayout=componentsFactory.createComponent(HBoxLayout.class);
                hBoxLayout.setSpacing(true);
                Label label=componentsFactory.createComponent(Label.class);
                label.setValue(messages.getMainMessage(componentLabel));
                hBoxLayout.add(label);

                HasValue control=null;

                switch (fieldDescription.type()){

                    case DATE:
                        control=createDateField(field,condition, DateField.Resolution.DAY);
                        break;

                    case DATE_TIME:
                        control=createDateField(field,condition, DateField.Resolution.HOUR);
                        break;

                    case INTEGER:
                        control=createIntegerField(field,condition);
                        break;

                    case TIME:
                        control=createTimeField(field,condition, false);
                        break;

                    case SELECT_MANY_STRINGS:
                        control=createTokenList(field,condition,fieldDescription);
                        break;

                    case SELECT_ONE_STRING:
                        control=createDropList(field,condition,fieldDescription);
                        break;
                }

                control.addValueChangeListener(event->{
                    updateDisplayedValue(condition);

                });

                if(control!=null) hBoxLayout.add(control);

                vbox.add(hBoxLayout);
            }
        });

        return vbox;
    }

    private void updateDisplayedValue(Condition condition){
        conditionsWithErrors.remove(condition);

        String stateDescription=condition.makeStateDescription();

        JustTransient correspondingTransient=resultConditionTransientMap.get(condition);
        if((stateDescription==null)||("".equals(stateDescription))||("null".equalsIgnoreCase(stateDescription))){
            correspondingTransient.setName(messages.getMainMessage(condition.getClass().getSimpleName()));
        }
        else if(correspondingTransient!=null){
            correspondingTransient.setName(String.format("%s: %s",
                    messages.getMainMessage(condition.getClass().getSimpleName()),
                    condition.makeStateDescription()));
        }

        selectedItemsTree.repaint();
    }

    private LookupField createDropList(java.lang.reflect.Field field, Condition condition, Condition.FieldDescription fieldDescription) {
        LookupField lookupField=componentsFactory.createComponent(LookupField.class);
        lookupField.setOptionsList(Arrays.asList(fieldDescription.options()));
        lookupField.setNullOptionVisible(false);
        try {
            lookupField.setValue(field.get(condition));
        } catch (Exception ignored) {}

        lookupField.addValueChangeListener(event->{
            try {
                field.set(condition,event.getValue());
            } catch (Exception ignored) {}
        });

        return lookupField;
    }

    private DateField createDateField(java.lang.reflect.Field field, Condition condition, DateField.Resolution resolution){
        DateField dateField=componentsFactory.createComponent(DateField.class);
        dateField.setResolution(resolution);
        try {
            dateField.setValue(field.get(condition));
        } catch (Exception ignored) {}
        dateField.addValueChangeListener(event->{
            try {
                field.set(condition,event.getValue());
            } catch (Exception ignored) {}
        });

        return dateField;
    }

    private TextField createIntegerField(java.lang.reflect.Field field, Condition condition){
        TextField textField=componentsFactory.createComponent(TextField.class);
        try {
            textField.setValue(field.get(condition));
        } catch (Exception ignored) {}
        textField.addValueChangeListener(event->{
            try {
                if (event.getValue() != null) {
                    int value=Integer.valueOf((String)event.getValue());
                    field.set(condition,value);
                }
                else{
                    field.set(condition,null);
                }

            }catch (NumberFormatException e){
                textField.setValue(event.getPrevValue());
            }
            catch (Exception ignored) {}
        });
        return textField;
    }

    private TimeField createTimeField(java.lang.reflect.Field field, Condition condition,boolean showSeconds){
        TimeField timeField=componentsFactory.createComponent(TimeField.class);
        timeField.setShowSeconds(showSeconds);
        try {
            timeField.setValue(field.get(condition));
        } catch (Exception ignored) {}
        timeField.addValueChangeListener(event->{
            try {
                field.set(condition,event.getValue());
            } catch (Exception ignored) {}
        });

        return timeField;

    }

    private TokenList createTokenList(java.lang.reflect.Field field, Condition condition, Condition.FieldDescription fieldDescription){
        TokenList tokenList=componentsFactory.createComponent(TokenList.class);
        tokenList.setOptionsList(Arrays.asList(fieldDescription.options()));

        CollectionDatasource<JustTransient,UUID> ds=DsBuilder.create()
                .setJavaClass(JustTransient.class)
                .setAllowCommit(false)
                .setRefreshMode(CollectionDatasource.RefreshMode.NEVER)
                .buildCollectionDatasource();

        List<JustTransient> transientList=new ArrayList<>();
        Map<JustTransient,String> transientStringMap=new HashMap<>();
        Map<String,JustTransient> stringJustTransientMap=new HashMap<>();
        Arrays.asList(fieldDescription.options()).forEach(item->{
            JustTransient justTransient=new JustTransient();
            justTransient.setName(messages.getMainMessage(item));
            transientList.add(justTransient);
            transientStringMap.put(justTransient,item);
            stringJustTransientMap.put(item,justTransient);
        });
        tokenList.setDatasource(ds);
        tokenList.setOptionsList(transientList);

        try {
            List<String> value= (List<String>) field.get(condition);
            value.forEach(item-> ds.addItem(stringJustTransientMap.get(item)));
        } catch (Exception ignored) {}

        tokenList.getDatasource().addCollectionChangeListener(event->{
            try {
                Collection<JustTransient> transientValue= ds.getItems();
                if(transientValue==null){
                    field.set(condition,Collections.EMPTY_LIST);
                    return;
                }
                List<String> value=new ArrayList<>();
                transientValue.forEach(item-> value.add(transientStringMap.get(item)));
                field.set(condition,value);

                updateDisplayedValue(condition);
                selectedItemsTree.repaint();
            } catch (Exception ignored) {}
        });

        return tokenList;
    }

    public void onOkClick() {
        if(validateConditionsFields()){
            close("ok");
        }
        else{
            showNotification(getMessage("fix_marked_conditions"),NotificationType.ERROR);
            selectedItemsTree.repaint();
        }
    }

    public void onCancelClick() {
        close("cancel");
    }
}