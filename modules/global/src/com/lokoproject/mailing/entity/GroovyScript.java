package com.lokoproject.mailing.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import com.haulmont.cuba.core.entity.StandardEntity;
import javax.persistence.Column;
import javax.persistence.Lob;
import com.haulmont.chile.core.annotations.NamePattern;
import com.haulmont.cuba.core.entity.annotation.Listeners;

@NamePattern("%s|name")
@Table(name = "MAILING_GROOVY_SCRIPT")
@Entity(name = "mailing$GroovyScript")
public class GroovyScript extends StandardEntity {
    private static final long serialVersionUID = 2073393875941883523L;

    @Lob
    @Column(name = "SCRIPT")
    protected String script;

    @Column(name = "SCRIP_VERSION")
    protected Integer scripVersion;

    @Column(name = "SCRIPT_VERSION_COMMENT")
    protected String scriptVersionComment;

    @Column(name = "RETURN_TYPE")
    protected Integer returnType;

    @Column(name = "ENTITY_TYPE")
    protected String entityType;

    @Column(name = "NAME")
    protected String name;

    public void setScripVersion(Integer scripVersion) {
        this.scripVersion = scripVersion;
    }

    public Integer getScripVersion() {
        return scripVersion;
    }

    public void setScriptVersionComment(String scriptVersionComment) {
        this.scriptVersionComment = scriptVersionComment;
    }

    public String getScriptVersionComment() {
        return scriptVersionComment;
    }


    public void setReturnType(DataType returnType) {
        this.returnType = returnType == null ? null : returnType.getId();
    }

    public DataType getReturnType() {
        return returnType == null ? null : DataType.fromId(returnType);
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }


    public void setScript(String script) {
        this.script = script;
    }

    public String getScript() {
        return script;
    }


}