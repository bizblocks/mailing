package com.lokoproject.mailing.notification.template.element;

import java.util.List;

/**
 * @author Antonlomako. created on 02.01.2019.
 * нужен при объединении шаблонов от разных рассылок. Из контент блоков с одинаковыми идами останется только блок с наивысшим mergePriority
 */
public class ContentBlock extends AbstractContainerElement{
    private Integer mergePriority;

    public Integer getMergePriority() {
        return mergePriority;
    }

    public void setMergePriority(Integer mergePriority) {
        this.mergePriority = mergePriority;
    }
}
