package com.lokoproject.mailing.notification.template.element;

/**
 * @author Antonlomako. created on 19.01.2019.
 */
public interface TemplateContainerElement extends TemplateElement {
    java.util.List<TemplateElement> getChildren();
    TemplateElement getChild(String id);
    TemplateElement getChild(int index);
    void addChild(TemplateElement child);
    void addChild(TemplateElement child,int index);
    void removeChild(String id);
    void removeChild(int index);
    void removeChild(TemplateElement child);
}
