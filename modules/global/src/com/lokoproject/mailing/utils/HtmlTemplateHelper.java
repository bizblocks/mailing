package com.lokoproject.mailing.utils;

import com.lokoproject.mailing.notification.template.element.List;

/**
 * @author Antonlomako. created on 29.12.2018.
 */
public class HtmlTemplateHelper {

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

    public String buildTable(){
        return null;
    }

    public static String buildList(List list){
        StringBuilder sb=new StringBuilder();
        list.getElements().forEach(item->{
            sb.append(wrapInTag(item,"li"));
        });

        String listTag;
        if(list.isOrdered()) {
            listTag="ol";
        }
        else{
            listTag="ul";
        }

        String result=wrapInTag(sb.toString(),listTag);

        if(list.getKeySymbol()!=null){
            try {
                result=addAttribute(result,"type",list.getKeySymbol());
            } catch (UtilException e) {
                return result;
            }
        }
        return result;
    }
}
