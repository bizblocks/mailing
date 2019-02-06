package com.lokoproject.mailing.notification.template.element;

import java.util.ArrayList;

/**
 * @author Antonlomako. created on 03.01.2019.
 */
public class List extends AbstractTemplateElement {
    private java.util.List<String> elements=new ArrayList<>();
    private boolean ordered=false;
    private String keySymbol;

    public List addElement(String element){
        elements.add(element);
        return this;
    }

    public java.util.List<String> getElements() {
        return elements;
    }

    public void setElements(java.util.List<String> elements) {
        this.elements = elements;
    }

    public boolean isOrdered() {
        return ordered;
    }

    public void setOrdered(boolean ordered) {
        this.ordered = ordered;
    }

    public String getKeySymbol() {
        return keySymbol;
    }

    public void setKeySymbol(String keySymbol) {
        this.keySymbol = keySymbol;
    }
}
