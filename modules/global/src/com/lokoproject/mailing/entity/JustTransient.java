package com.lokoproject.mailing.entity;

import com.haulmont.chile.core.annotations.MetaClass;
import com.haulmont.cuba.core.entity.BaseUuidEntity;

import javax.persistence.Transient;
import com.haulmont.chile.core.annotations.MetaProperty;
import com.haulmont.chile.core.annotations.NamePattern;

@NamePattern("%s|name")
@MetaClass(name = "mailing$JustTransient")
public class JustTransient extends BaseUuidEntity {
    private static final long serialVersionUID = -4429409295854281902L;

    @MetaProperty
    protected JustTransient parent;

    @MetaProperty
    protected String valueTwo;

    @MetaProperty
    protected String name;

    @MetaProperty
    protected String valueOne;

    public void setValueTwo(String valueTwo) {
        this.valueTwo = valueTwo;
    }

    public String getValueTwo() {
        return valueTwo;
    }


    public void setValueOne(String valueOne) {
        this.valueOne = valueOne;
    }

    public String getValueOne() {
        return valueOne;
    }


    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }


    public void setParent(JustTransient parent) {
        this.parent = parent;
    }

    public JustTransient getParent() {
        return parent;
    }



}