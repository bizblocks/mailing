package com.lokoproject.mailing.notification.template;

import com.haulmont.chile.core.model.MetaProperty;
import com.haulmont.cuba.core.entity.StandardEntity;
import com.lokoproject.mailing.notification.template.element.*;
import com.lokoproject.mailing.notification.template.style.ElementStyle;

import java.util.*;
import java.util.List;

/**
 * @author Antonlomako. created on 02.01.2019.
 */
public class TemplateBuilder {

    /**
     * колонки таблицы для каждого поля сущности
     * @param data набор сущностей
     */
    public static TableBuilder createTableByEntities(List<StandardEntity> data){
        return new TableBuilder().withEntityData(data);
    }

    /**
     * создание таблицы по карте значений.
     * @param data набор карт значений. ключи - идентификаторы колонок
     */
    public static TableBuilder createTableBuilder(List<Map<String,String>> data){
        return new TableBuilder().withData(data);
    }

    public static ContentBlockBuilder createTemplateBuilder(){
        return new ContentBlockBuilder();
    }


    public ContentBlockBuilder createContentBlockBuilder(){
        return new ContentBlockBuilder();
    }

    public static com.lokoproject.mailing.notification.template.element.List createList(List<String> elements){
        com.lokoproject.mailing.notification.template.element.List result=new com.lokoproject.mailing.notification.template.element.List();
        result.setElements(elements);
        return result;
    }

    private static abstract class AbstractBuilder{

        protected boolean buildEmptyElement=false; //если не хватает данных для  нормального построения элемента;

        protected abstract TemplateElement getResult();
        protected   abstract TemplateElement createElement();

        public  TemplateElement build(){
            if(!buildEmptyElement) return createElement();
            else return new Text();
        }
        public  AbstractBuilder withId(String id){
            getResult().setId(id);
            return this;
        }
        public AbstractBuilder withStyle(ElementStyle elementStyle){
            getResult().addStyle(elementStyle);
            return this;
        }
    }

    public interface CellValueGenerator{
        String generateCellValue(StandardEntity entity);
    }

    public static class TableBuilder extends AbstractBuilder {
        private Table table=new Table();
        private List<Map<String,String>> data;
        private List<StandardEntity> entityData;
        private Map<String,CellValueGenerator> generatedColumnsMap=new LinkedHashMap<>();

        private ElementStyleProvider headerStyleProvider;
        private ElementStyleProvider rowStyleProvider;
        private ElementStyleProvider columnStyleProvider;

        private StandardEntity source;

        private List<String> columns;
        private Map<String,String> columnFormatMap;
        private Map<String,String> headerLocalizationMap;

        TableBuilder withData(List<Map<String,String>> data){
            if((data==null)||(data.size()==0)){
                buildEmptyElement=true;
            }
            this.data=data;
            return this;
        }
        TableBuilder withEntityData(List<StandardEntity> data){
            this.entityData=data;
            if((data==null)||(data.size()==0)){
               buildEmptyElement=true;
            }
            else{
                source=data.get(0);
            }
            return this;
        }

        public TableBuilder withRowStyleProvider(ElementStyleProvider styleProvider){
            this.rowStyleProvider=styleProvider;
            return this;
        }
        public TableBuilder withColumnStyleProvider(ElementStyleProvider styleProvider){
            this.columnStyleProvider=styleProvider;
            return this;
        }
        public TableBuilder withHeaderStyleProvider(ElementStyleProvider styleProvider){
            this.headerStyleProvider=styleProvider;
            return this;
        }
        public TableBuilder withColumns(List<String> columns){
            this.columns=columns;
            return this;
        }
        public TableBuilder withColumnFormat(Map<String,String> columnFormatMap){
            this.columnFormatMap=columnFormatMap;
            return this;
        }
        public TableBuilder withGeneratedColumn(String columnId,CellValueGenerator cellValueGenerator){
            if(source!=null){
                generatedColumnsMap.put(columnId,cellValueGenerator);
            }
            return this;
        }

        @Override
        protected TemplateElement getResult() {
            return table;
        }

        @Override
        protected TemplateElement createElement() {

            //создание заголовков колонок
            List<String> columns;
            if(this.columns!=null) columns=this.columns;
            else{
                columns=new ArrayList<>();
                if(source!=null) {
                    Collection<MetaProperty> properties = source.getMetaClass().getProperties();
                    properties.forEach(item->{
                        columns.add(item.getName());
                    });
                }
                else{
                    data.get(0).keySet().forEach(columns::add);
                }
            }
            TableRow header=new TableRow();
            columns.forEach(item->{
                TableCell headerCell=new TableCell();
                if((headerLocalizationMap!=null)&&(headerLocalizationMap.get(item)!=null)){
                    headerCell.setContent(headerLocalizationMap.get(item));
                }
                else{
                    headerCell.setContent(item);
                }
                header.getCells().add(headerCell);
            });
            table.setHeader(header);

            //создание строк
            if(entityData!=null) {
                entityData.forEach(entity -> {
                    TableRow row=new TableRow();
                    columns.forEach(column->{
                        row.getCells().add(getFormattedCell(entity.getValueEx(column),column));
                    });
                    table.getRows().add(row);
                });
            }
            else{
                data.forEach(valueMap->{
                    TableRow row=new TableRow();
                    columns.forEach(column->{
                        row.getCells().add(getFormattedCell(valueMap.get(column),column));
                    });
                    table.getRows().add(row);
                });
            }

            return table;
        }



        private TableCell getFormattedCell(Object object, String columnId){

            TableCell result=new TableCell();
            if (object==null) return result;

            String stringValue=null;
            Object outputObject=object;
            Number numberValue=null;
            Boolean booleanValue=null;
            if(object instanceof StandardEntity){
                stringValue=((StandardEntity)object).getInstanceName();
                outputObject=stringValue;
            }
            else if(object instanceof String){
                stringValue= (String) object;
            }
            else if(object instanceof Number){
                numberValue= (Number) object;
            }
            else if(object instanceof Boolean){
                booleanValue= (Boolean) object;
            }

            if((columnFormatMap!=null)&&(columnFormatMap.get(columnId)!=null)){
                result.setContent(String.format(columnFormatMap.get(columnId),outputObject));
            }
            else{
                if(stringValue!=null) result.setContent(stringValue);
                else if(numberValue!=null) result.setContent(String.valueOf(numberValue));
                else if(booleanValue!=null)result.setBooleanVale(booleanValue);
                else result.setContent(object.toString());
            }
            return result;
        }
    }

    public static class ContentBlockBuilder extends AbstractBuilder{
        private ContentBlock contentBlock =new ContentBlock();

        public ContentBlockBuilder withChild(TemplateElement templateElement){
            contentBlock.addChild(templateElement);
            return this;
        }

        public ContentBlockBuilder withMergePriority(int mergePriority){
            contentBlock.setMergePriority(mergePriority);
            return this;
        }

        @Override
        protected TemplateElement getResult() {
            return contentBlock;
        }

        @Override
        public TemplateElement createElement() {
            return contentBlock;
        }
    }

    public static class ListBuilder extends AbstractBuilder{
        private com.lokoproject.mailing.notification.template.element.List list=new com.lokoproject.mailing.notification.template.element.List();

        @Override
        protected TemplateElement getResult() {
            return list;
        }

        @Override
        public TemplateElement createElement() {
            return null;
        }

        @Override
        public ListBuilder withStyle(ElementStyle elementStyle) {
            return null;
        }
    }

    public static class TextBuilder extends AbstractBuilder{
        private Text text=new Text();

        @Override
        protected TemplateElement getResult() {
            return text;
        }

        @Override
        public TemplateElement createElement() {
            return null;
        }

        @Override
        public TextBuilder withStyle(ElementStyle elementStyle) {
            return null;
        }
    }

    public static interface ElementStyleProvider{
        ElementStyle calculateStyle(Object data);
    }
}
