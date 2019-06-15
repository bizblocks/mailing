package com.lokoproject.mailing.entity;

import javax.persistence.*;

import com.haulmont.chile.core.annotations.MetaProperty;
import com.haulmont.cuba.core.entity.StandardEntity;
import com.lokoproject.mailing.conditions.Condition;
import com.haulmont.cuba.core.entity.annotation.Listeners;
import com.lokoproject.mailing.conditions.OrCondition;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.UUID;
import com.haulmont.chile.core.annotations.NamePattern;
import java.util.List;

@NamePattern("%s|name")
@Listeners("mailing_MailingEntityListener")
@Table(name = "MAILING_MAILING")
@Entity(name = "mailing$Mailing")
public class Mailing extends StandardEntity {
    private static final long serialVersionUID = -2020259493447278898L;

    @JoinTable(name = "MAILING_MAILING_GROOVY_SCRIPT_LINK",
        joinColumns = @JoinColumn(name = "MAILING_ID"),
        inverseJoinColumns = @JoinColumn(name = "GROOVY_SCRIPT_ID"))
    @ManyToMany
    protected List<GroovyScript> mailingTargetScript;




    @Column(name = "ENTITY_TYPE_FOR_PERSONAL_SETTINGS")
    protected String entityTypeForPersonalSettings;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ORIGIN_MAILING_ID")
    protected Mailing originMailing;


    @Column(name = "ENTITY_ID_FOR_PERSONAL_SETTINGS")
    protected UUID entityIdForPersonalSettings;

    @Column(name = "USE_DEFAULT_MAILING")
    @PersonalizedOnly
    protected Boolean useDefaultMailing;

    @Column(name = "ACTIVATED")
    @Personalized
    protected Boolean activated=true;

    @Column(name = "STRING_ID")
    protected String stringId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CONSOLIDATION_GROOVY_ID")
    @Personalized
    protected GroovyScript consolidationGroovy;

    @Lob
    @Column(name = "CONSOLIDATION_JSON")
    @Personalized
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
    @JoinColumn(name = "ADAPTER_FOR_MAILING_TARGET_SCREEN_ID")
    protected GroovyScript adapterForMailingTargetScreen;

    @Column(name = "MAILING_PERFORMERS", length = 1000)
    @Personalized
    protected String mailingPerformers;

    @Column(name = "NAME")
    protected String name;

    @Transient
    @Personalized
    private Condition consolidationCondition;

    public void setUseDefaultMailing(Boolean useDefaultMailing) {
        this.useDefaultMailing = useDefaultMailing;
    }

    public Boolean getUseDefaultMailing() {
        return useDefaultMailing;
    }


    public void setEntityIdForPersonalSettings(UUID entityIdForPersonalSettings) {
        this.entityIdForPersonalSettings = entityIdForPersonalSettings;
    }

    public UUID getEntityIdForPersonalSettings() {
        return entityIdForPersonalSettings;
    }


    public void setEntityTypeForPersonalSettings(String entityTypeForPersonalSettings) {
        this.entityTypeForPersonalSettings = entityTypeForPersonalSettings;
    }

    public String getEntityTypeForPersonalSettings() {
        return entityTypeForPersonalSettings;
    }


    public void setOriginMailing(Mailing originMailing) {
        this.originMailing = originMailing;
    }

    public Mailing getOriginMailing() {
        return originMailing;
    }



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



    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<GroovyScript> getMailingTargetScript() {
        return mailingTargetScript;
    }

    public void setMailingTargetScript(List<GroovyScript> mailingTargetScript) {
        this.mailingTargetScript = mailingTargetScript;
    }

    public void setAdapterForMailingTargetScreen(GroovyScript adapterForMailingTargetScreen) {
        this.adapterForMailingTargetScreen = adapterForMailingTargetScreen;
    }

    public GroovyScript getAdapterForMailingTargetScreen() {
        return adapterForMailingTargetScreen;
    }


    public void setConsolidationCondition(Condition consolidationCondition) {
        this.consolidationCondition = consolidationCondition;
    }

    public Condition getConsolidationCondition() {
        return consolidationCondition;
    }


    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Personalized{}

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface PersonalizedOnly{}
}