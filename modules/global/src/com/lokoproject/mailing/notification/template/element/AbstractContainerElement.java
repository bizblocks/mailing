package com.lokoproject.mailing.notification.template.element;

import java.util.*;
import java.util.List;

/**
 * @author Antonlomako. created on 19.01.2019.
 */
public abstract class AbstractContainerElement extends AbstractTemplateElement implements TemplateContainerElement {

    List<TemplateElement> children=new ArrayList<>();

    @Override
    public List<TemplateElement> getChildren() {
        return Collections.unmodifiableList(children);
    }

    @Override
    public TemplateElement getChild(String id) {
        for(TemplateElement element:children){
            if(element.getId().equals(id)) return element;
        }
        return null;
    }

    @Override
    public TemplateElement getChild(int index) {
        if((index>=children.size())||(index<0)) return null;
        return children.get(index);
    }

    @Override
    public void addChild(TemplateElement child) {
        children.add(child);
    }

    @Override
    public void addChild(TemplateElement child, int index) {
        if((index<=children.size())&&(index>=0)){
            children.add(index,child);
        }
        else {
            children.add(child);
        }
    }

    @Override
    public void removeChild(String id) {
        TemplateElement elementToRemove=getChild(id);
        if(elementToRemove!=null) children.remove(elementToRemove);
    }

    @Override
    public void removeChild(int index) {
        TemplateElement elementToRemove=getChild(index);
        if(elementToRemove!=null) children.remove(elementToRemove);
    }

    @Override
    public void removeChild(TemplateElement child) {
        children.remove(child);
    }
}
