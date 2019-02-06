package com.lokoproject.mailing.notification.template;



import com.lokoproject.mailing.notification.template.element.TemplateElement;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Antonlomako. created on 09.12.2018.
 */
public class Template {
    private String theme;
    private String description;
    private String iconName;
    private List<TemplateElement> content=new ArrayList<>();

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

    public List<TemplateElement> getContent() {
        return content;
    }

    public void setContent(List<TemplateElement> content) {
        this.content = content;
    }
}
