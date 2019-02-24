package com.lokoproject.mailing.utils;

import com.lokoproject.mailing.notification.template.element.*;

/**
 * @author Antonlomako. created on 29.12.2018.
 */
public class HtmlTemplateHelper {

    private static String tableStyle="<style>\n" +
            "table {\n" +
            "  font-family: \"Trebuchet MS\", Arial, Helvetica, sans-serif;\n" +
            "  border-collapse: collapse;\n" +
            "  width: 100%;\n" +
            "}\n" +
            "\n" +
            "table td, th {\n" +
            "  border: 1px solid #ddd;\n" +
            "  padding: 8px;\n" +
            "}\n" +
            "\n" +
            "table tr:nth-child(even){background-color: #f2f2f2;}\n" +
            "\n" +
            "table tr:hover {background-color: #ddd;}\n" +
            "\n" +
            "table th {\n" +
            "  padding-top: 12px;\n" +
            "  padding-bottom: 12px;\n" +
            "  text-align: left;\n" +
            "  background-color: #4CAF50;\n" +
            "  color: white;\n" +
            "}\n" +
            "</style>";

    public static String wrapInTag(String content,String tag){
        return "<"+tag+">"+content+"</"+tag+">";
    }

    public static String addStyle(String content,String style) throws UtilException {
        return addAttribute(content,"style",style);

    }

    public static String addAttribute(String content,String attrName,String attrContent) throws UtilException {
        int openIndex,closeIndex,styleIndex;
        String result;
        openIndex=content.indexOf('<');
        closeIndex=content.indexOf('>');
        if((openIndex==-1)||(closeIndex==-1)||(closeIndex<openIndex)) throw new UtilException("content must be valid html: "+content);

        styleIndex=content.substring(openIndex,closeIndex).indexOf(attrName+"='");
        if(styleIndex!=-1){
            result=content.substring(0,closeIndex)+" "+attrName+"='"+attrContent+";'"+content.substring(closeIndex);
        }
        else{
            result=content.substring(0,styleIndex+7)+attrContent+";"+content.substring(styleIndex+7);
        }

        return result;

    }

    public static StringBuilder wrapInTag(StringBuilder contentBuilder,String tag){
        contentBuilder.insert(0,">").insert(0,tag).insert(0,"<").append("</").append(tag).append(">");
        return contentBuilder;
    }

    public static void addWrappedContent(StringBuilder contentBuilder,String content,String tag){
        contentBuilder.append("<").append(tag).append(">").append(content).append("</").append(tag).append(">");
    }

    public static StringBuilder addStyle(StringBuilder contentBuilder,String style) throws UtilException {
        return addAttribute(contentBuilder,"style",style);

    }

    public static StringBuilder addAttribute(StringBuilder contentBuilder,String attrName,String attrContent) throws UtilException {
        int openIndex,closeIndex,styleIndex;
        StringBuilder result=new StringBuilder();
        openIndex=contentBuilder.indexOf("<");
        closeIndex=contentBuilder.indexOf(">");
        if((openIndex==-1)||(closeIndex==-1)||(closeIndex<openIndex)) throw new UtilException("content must be valid html: "+contentBuilder.toString());

        styleIndex=contentBuilder.substring(openIndex,closeIndex).indexOf(attrName+"='");
        if(styleIndex!=-1){
            result.append(contentBuilder.substring(0,closeIndex))
                    .append(" ")
                    .append(attrName)
                    .append("='")
                    .append(attrContent)
                    .append(";'")
                    .append(contentBuilder.substring(closeIndex));
        }
        else{
            result.append(contentBuilder.substring(0,styleIndex+7))
                    .append(attrContent)
                    .append(";")
                    .append(contentBuilder.substring(styleIndex+7));
        }

        return result;

    }

    public static StringBuilder buildTable(StringBuilder templateBuilder,Table table){
        StringBuilder tableBuilder=new StringBuilder();

        table.getHeader().getCells().forEach(item->{
            addWrappedContent(tableBuilder,item.getContent(),"th");
        });
        wrapInTag(tableBuilder,"tr");

        table.getRows().forEach(row->{
            StringBuilder rowBuilder=new StringBuilder();
            row.getCells().forEach(cell->{
                addWrappedContent(rowBuilder,cell.getContent(),"td");
            });
            wrapInTag(rowBuilder,"tr");
            tableBuilder.append(rowBuilder);
        });

        wrapInTag(tableBuilder,"table");
        if(templateBuilder!=null) templateBuilder.append(tableBuilder);
        return tableBuilder;

    }

    public static StringBuilder buildList(StringBuilder templateBuilder,List list){
        StringBuilder listBuilder=new StringBuilder();
        list.getElements().forEach(item->{
            listBuilder.append(wrapInTag(item,"li"));
        });

        String listTag;
        if(list.isOrdered()) {
            listTag="ol";
        }
        else{
            listTag="ul";
        }

        wrapInTag(listBuilder,listTag);

        if(list.getKeySymbol()!=null){
            try {
                addAttribute(listBuilder,"type",list.getKeySymbol());
            } catch (UtilException e) {
                if(templateBuilder!=null) templateBuilder.append(listBuilder);
                return listBuilder;
            }
        }
        if(templateBuilder!=null) templateBuilder.append(listBuilder);
        return listBuilder;
    }

    public static StringBuilder buildText(StringBuilder templateBuilder, Text text){
        StringBuilder resultBuilder=templateBuilder==null? new StringBuilder():templateBuilder;
        addWrappedContent(resultBuilder,text.getContent(),"p");
        return resultBuilder;
    }

    public static StringBuilder buildHeader(StringBuilder templateBuilder, Header header){
        StringBuilder resultBuilder=templateBuilder==null? new StringBuilder():templateBuilder;
        addWrappedContent(resultBuilder,header.getContent(),"p");
        return resultBuilder;
    }

    public static StringBuilder buildTemplateRecur(StringBuilder templateBuilder,TemplateElement templateElement){
        StringBuilder resultBuilder=templateBuilder==null? new StringBuilder():templateBuilder;

        if(templateElement instanceof TemplateContainerElement){
            TemplateContainerElement containerElement= (TemplateContainerElement) templateElement;
            containerElement.getChildren().forEach(item->{
                buildTemplateRecur(resultBuilder,item);
            });
        }
        else{
            if(templateElement instanceof Table){
                buildTable(resultBuilder,(Table)templateElement);
            }
            else if(templateElement instanceof List){
                buildList(resultBuilder, (List) templateElement);
            }
            else if(templateElement instanceof Header){
                buildHeader(resultBuilder, (Header) templateElement);
            }
            else if(templateElement instanceof Text){
                buildText(resultBuilder, (Text) templateElement);
            }
        }

        return resultBuilder;
    }

    public static String buildList(List list) {
        return buildList(null,list).toString();
    }
}
