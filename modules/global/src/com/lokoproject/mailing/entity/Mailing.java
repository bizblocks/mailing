package com.lokoproject.mailing.entity;

import javax.persistence.*;

import com.haulmont.cuba.core.entity.StandardEntity;
import com.lokoproject.mailing.conditions.Condition;
import com.haulmont.cuba.core.entity.annotation.Listeners;
import com.lokoproject.mailing.conditions.OrCondition;

@Listeners("mailing_MailingEntityListener")
@Table(name = "MAILING_MAILING")
@Entity(name = "mailing$Mailing")
public class Mailing extends StandardEntity {
    private static final long serialVersionUID = -2020259493447278898L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MAILING_TARGET_SCRIPT_ID")
    protected GroovyScript mailingTargetScript;

    @Column(name = "ACTIVATED")
    protected Boolean activated=true;

    @Column(name = "STRING_ID")
    protected String stringId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CONSOLIDATION_GROOVY_ID")
    protected GroovyScript consolidationGroovy;

    @Lob
    @Column(name = "CONSOLIDATION_JSON")
    protected String consolidationConditionJson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "NOTIFICATION_BUILD_SCRIPT_ID")
    protected GroovyScript notificationBuildScript;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OBJECT_FILTER_SCRIPT_ID")
    protected GroovyScript objectFilterScript;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FINAL_CHECK_SCRIPT_ID")
    protected GroovyScript finalCheckScript;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FINAL_DECORATION_SCRIPT_ID")
    protected GroovyScript finalDecorationScript;

    @Column(name = "MAILING_PERFORMERS", length = 1000)
    protected String mailingPerformers;

    @Column(name = "NAME")
    protected String name;

    @Transient
    private OrCondition consolidationCondition=new OrCondition();

    public void setActivated(Boolean activated) {
        this.activated = activated;
    }

    public Boolean getActivated() {
        return activated;
    }


    public void setStringId(String stringId) {
        this.stringId = stringId;
    }

    public String getStringId() {
        return stringId;
    }


    public void setMailingPerformers(String mailingPerformers) {
        this.mailingPerformers = mailingPerformers;
    }

    public String getMailingPerformers() {
        return mailingPerformers;
    }


    public void setConsolidationGroovy(GroovyScript consolidationGroovy) {
        this.consolidationGroovy = consolidationGroovy;
    }

    public GroovyScript getConsolidationGroovy() {
        return consolidationGroovy;
    }


    public void setConsolidationConditionJson(String consolidationConditionJson) {
        this.consolidationConditionJson = consolidationConditionJson;
    }

    public String getConsolidationConditionJson() {
        return consolidationConditionJson;
    }


    public void setNotificationBuildScript(GroovyScript notificationBuildScript) {
        this.notificationBuildScript = notificationBuildScript;
    }

    public GroovyScript getNotificationBuildScript() {
        return notificationBuildScript;
    }


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

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }


    public Condition getConsolidationCondition() {
        return consolidationCondition;
    }

    public void setConsolidationCondition(OrCondition consolidationCondition) {
        this.consolidationCondition = consolidationCondition;
    }
}