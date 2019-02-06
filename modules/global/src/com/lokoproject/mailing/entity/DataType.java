package com.lokoproject.mailing.entity;

import com.haulmont.chile.core.datatypes.impl.EnumClass;

import javax.annotation.Nullable;


public enum DataType implements EnumClass<Integer> {

    VOID(10),
    STRING(20),
    BOOLEAN(70),
    INTEGER(30),
    DOUBLE(40),
    DATE(50),
    COLLECTION(60);

    private Integer id;

    DataType(Integer value) {
        this.id = value;
    }

    public Integer getId() {
        return id;
    }

    @Nullable
    public static DataType fromId(Integer id) {
        for (DataType at : DataType.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}