package com.lokoproject.mailing.notification.template.element;

import com.lokoproject.mailing.notification.template.style.ElementStyle;

import java.io.Serializable;

/**
 * @author Antonlomako. created on 01.01.2019.
 */
public interface TemplateElement extends Serializable {
    void addStyle(ElementStyle elementStyle);
    String getId();
    void setId(String id);
}
