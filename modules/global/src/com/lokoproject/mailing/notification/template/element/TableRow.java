package com.lokoproject.mailing.notification.template.element;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

/**
 * @author Antonlomako. created on 14.01.2019.
 */
public class TableRow implements Serializable {
    private java.util.List<TableCell> cells=new ArrayList<>();
    private UUID entityId;
    private String entityClass;

    public java.util.List<TableCell> getCells() {
        return cells;
    }

    public void setCells(java.util.List<TableCell> cells) {
        this.cells = cells;
    }

    public UUID getEntityId() {
        return entityId;
    }

    public void setEntityId(UUID entityId) {
        this.entityId = entityId;
    }

    public String getEntityClass() {
        return entityClass;
    }

    public void setEntityClass(String entityClass) {
        this.entityClass = entityClass;
    }
}
