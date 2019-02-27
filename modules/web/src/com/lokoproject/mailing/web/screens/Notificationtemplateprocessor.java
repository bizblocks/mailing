package com.lokoproject.mailing.web.screens;

import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.chile.core.model.Session;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.BaseAction;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.DsBuilder;
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory;
import com.haulmont.cuba.security.entity.User;
import com.lokoproject.mailing.entity.JustTransient;
import com.lokoproject.mailing.notification.template.TemplateWrapper;
import com.lokoproject.mailing.notification.template.element.*;
import com.lokoproject.mailing.notification.template.element.Link;
import com.lokoproject.mailing.notification.template.element.Table;
import com.lokoproject.mailing.service.DaoService;
import com.lokoproject.mailing.utils.HtmlTemplateHelper;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public class Notificationtemplateprocessor extends AbstractWindow {

    @Inject
    private ComponentsFactory componentsFactory;

    @Inject
    private Metadata metadata;

    @Inject
    private DaoService daoService;

    private TemplateWrapper notificationTemplate;

    @Override
    public void init(Map<String,Object> params){
        notificationTemplate= (TemplateWrapper) params.get("notificationTemplate");

        if(notificationTemplate!=null){
            createComponentsByTemplate();
            setCaption(notificationTemplate.getTheme());
        }

    }

    private void createComponentsByTemplate() {
        VBoxLayout vBoxLayout=componentsFactory.createComponent(VBoxLayout.class);
        vBoxLayout.setWidth("100%");
        vBoxLayout.setSpacing(true);
        add(vBoxLayout);
        createComponentsByTemplateRecur(vBoxLayout,notificationTemplate);
    }

    private void createComponentsByTemplateRecur(Container layout,TemplateElement templateElement) {
        if(templateElement instanceof TemplateContainerElement){
            TemplateContainerElement container= (TemplateContainerElement) templateElement;
            container.getChildren().forEach(item->{
                createComponentsByTemplateRecur(layout,item);
            });
        }
        else{
            Component result=null;
            if(templateElement instanceof Header){
                result=createHeader((Header) templateElement);
            }
            else if(templateElement instanceof Link){
                result=createLink((Link) templateElement);
            }
            else if(templateElement instanceof List){
                result=createList((List) templateElement);
            }
            else if(templateElement instanceof Table){
                result=createTable((Table) templateElement);
            }
            if(result!=null){
                layout.add(result);
            }
        }
    }

    private Label createHeader(Header header){
        Label label=componentsFactory.createComponent(Label.class);
        label.setWidth("100%");
        label.setValue(HtmlTemplateHelper.wrapInTag(header.getContent(),"h2"));
        label.setHtmlEnabled(true);
        return label;
    }
    private com.haulmont.cuba.gui.components.Link createLink(Link link){
        com.haulmont.cuba.gui.components.Link linkComponent=componentsFactory.createComponent(com.haulmont.cuba.gui.components.Link.class);
        linkComponent.setUrl(link.getDestination());
        linkComponent.setRel(link.getContent());
        return linkComponent;
    }
    private Label createList(List list){
        Label label=componentsFactory.createComponent(Label.class);
        label.setWidth("100%");
        label.setHtmlEnabled(true);
        label.setValue(HtmlTemplateHelper.buildList(list));
        return label;
    }

    Map<com.haulmont.cuba.gui.components.Table,Map<TableRow,Entity>> mapToProcessTable=new HashMap<>();

    private com.haulmont.cuba.gui.components.Table createTable(Table table){
        com.haulmont.cuba.gui.components.Table tableComponent=componentsFactory.createComponent(com.haulmont.cuba.gui.components.Table.class);

        MetaClass metaClass;
        Boolean loadEntity;
        try{
            Session session = metadata.getSession();
            metaClass = session.getClassNN(table.getRows().get(0).getEntityClass());
            loadEntity=metaClass!=null;
        }
        catch (Exception e){
            loadEntity=false;
        }

        CollectionDatasource ds= DsBuilder.create()
                .setJavaClass(JustTransient.class)   //любая сущность подойдет, пусть будет юзер
                .setAllowCommit(false)
                .buildCollectionDatasource();

        Map<TableRow,Entity> tableRowEntityMap=new HashMap<>();
        Map<Entity,TableRow> entityTableRowMap=new HashMap<>();
        mapToProcessTable.put(tableComponent,tableRowEntityMap);


        table.getRows().forEach(item->{
            Entity entity= metadata.create(JustTransient.class);
            ds.addItem(entity);
            tableRowEntityMap.put(item,entity);
            entityTableRowMap.put(entity,item);
        });
        tableComponent.setDatasource(ds);
        ArrayList<com.haulmont.cuba.gui.components.Table.Column> columns=new ArrayList<>(tableComponent.getColumns());
        columns.forEach(tableComponent::removeColumn);

        //если строка связана с сущностью,то по двойному кику откроется редактор
        if(loadEntity){
            tableComponent.setItemClickAction(new BaseAction("click"){
                @Override
                public void actionPerform(Component component){
                    Entity selected=tableComponent.getSingleSelected();
                    TableRow row=entityTableRowMap.get(selected);
                    Entity entityToOpen=loadEntity(row);
                    openEditor(entityToOpen, WindowManager.OpenType.DIALOG);
                }
            });
        }

        if(table.getHeader()!=null) {
            table.getHeader().getCells().forEach(cell -> {
                final int cellIndex = table.getHeader().getCells().indexOf(cell);
                tableComponent.addGeneratedColumn(cell.getContent(), entity -> {
                    Label label = componentsFactory.createComponent(Label.class);
                    TableRow row = entityTableRowMap.get(entity);
                    label.setValue(row.getCells().get(cellIndex).getContent());
                    return label;
                });
            });
        }

        tableComponent.setWidth("100%");

        return tableComponent;
    }


    private Label createText(Text text){
        Label label=componentsFactory.createComponent(Label.class);
        label.setWidth("100%");
        label.setValue(text.getContent());
        return label;
    }

    private Entity loadEntity(TableRow item) {
        return daoService.getEntity(item.getEntityClass(),item.getEntityId().toString());
    }

}