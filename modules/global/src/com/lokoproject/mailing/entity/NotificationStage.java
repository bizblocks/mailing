package com.lokoproject.mailing.entity;

import com.haulmont.chile.core.datatypes.impl.EnumClass;

import javax.annotation.Nullable;


public enum NotificationStage implements EnumClass<Integer> {

    CONSOLIDATION(10),
    AFTER_CONSOLIDATION(20),
    PROCESSED(30),
    READ(40),
    UNREACHABLE_TARGET(50),
    REMOVED(60);

    private Integer id;

    NotificationStage(Integer value) {
        this.id = value;
    }

    public Integer getId() {
        return id;
    }

    @Nullable
    public static NotificationStage fromId(Integer id) {
        for (NotificationStage at : NotificationStage.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}