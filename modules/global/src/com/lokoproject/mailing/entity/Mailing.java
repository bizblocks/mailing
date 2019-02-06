package com.lokoproject.mailing.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Scripting;
import com.haulmont.cuba.security.entity.User;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.util.Eval;
import org.apache.commons.lang.StringUtils;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.util.*;

@Table(name = "MAILING_MAILING")
@Entity(name = "mailing$Mailing")
public class Mailing extends StandardEntity {
    private static final long serialVersionUID = -2020259493447278898L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MAILING_TARGET_SCRIPT_ID")
    protected GroovyScript mailingTargetScript;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OBJECT_FILTER_SCRIPT_ID")
    protected GroovyScript objectFilterScript;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FINAL_CHECK_SCRIPT_ID")
    protected GroovyScript finalCheckScript;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FINAL_DECORATION_SCRIPT_ID")
    protected GroovyScript finalDecorationScript;

    @Column(name = "MAILING_AGENTS")
    protected String mailingAgents;

    @Column(name = "NAME")
    protected String name;

    public void setMailingTargetScript(GroovyScript mailingTargetScript) {
        this.mailingTargetScript = mailingTargetScript;
    }

    public GroovyScript getMailingTargetScript() {
        return mailingTargetScript;
    }

    public void setObjectFilterScript(GroovyScript objectFilterScript) {
        this.objectFilterScript = objectFilterScript;
    }

    public GroovyScript getObjectFilterScript() {
        return objectFilterScript;
    }

    public void setFinalCheckScript(GroovyScript finalCheckScript) {
        this.finalCheckScript = finalCheckScript;
    }

    public GroovyScript getFinalCheckScript() {
        return finalCheckScript;
    }

    public void setFinalDecorationScript(GroovyScript finalDecorationScript) {
        this.finalDecorationScript = finalDecorationScript;
    }

    public GroovyScript getFinalDecorationScript() {
        return finalDecorationScript;
    }

    public void setMailingAgents(String mailingAgents) {
        this.mailingAgents = mailingAgents;
    }

    public String getMailingAgents() {
        return mailingAgents;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }



}