package com.lokoproject.mailing.notification.template.element;

import com.lokoproject.mailing.notification.template.style.ElementStyle;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Antonlomako. created on 03.01.2019.
 */
public abstract class AbstractTemplateElement implements TemplateElement {

    protected String id;

    protected List<ElementStyle> styleList=new ArrayList<>();

    @Override
    public void addStyle(ElementStyle elementStyle) {
        styleList.add(elementStyle);
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
