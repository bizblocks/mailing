package com.lokoproject.mailing.notification.template;

import com.lokoproject.mailing.notification.template.element.AbstractContainerElement;

/**
 * @author Antonlomako. created on 09.12.2018.
 */
public class TemplateWrapper extends AbstractContainerElement {
    private String theme;
    private String description;
    private String iconName;

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }
}
