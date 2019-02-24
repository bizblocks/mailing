package com.lokoproject.mailing.entity;

import javax.persistence.*;

import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.cuba.security.entity.User;
import com.lokoproject.mailing.notification.template.TemplateWrapper;

import java.util.HashSet;
import java.util.Set;
import java.util.Date;
import com.haulmont.cuba.core.entity.annotation.Listeners;

@Listeners("mailing_NotificationEntityListener")
@Table(name = "MAILING_NOTIFICATION")
@Entity(name = "mailing$Notification")
public class Notification extends StandardEntity {
    private static final long serialVersionUID = -3219109399113325120L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TARGET_ID")
    protected User target;

    @Lob
    @Column(name = "TEMPLATE_JSON")
    protected String templateJson;

    @Temporal(TemporalType.DATE)
    @Column(name = "SEND_DATE")
    protected Date sendDate;

    @Column(name = "STAGE")
    protected Integer stage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MAILING_ID")
    protected Mailing mailing;

    @Transient
    protected TemplateWrapper template;

    @Transient
    private Set<Object> objects=new HashSet<>();

    public void setTemplateJson(String templateJson) {
        this.templateJson = templateJson;
    }

    public String getTemplateJson() {
        return templateJson;
    }


    public void setSendDate(Date sendDate) {
        this.sendDate = sendDate;
    }

    public Date getSendDate() {
        return sendDate;
    }


    public void setStage(NotificationStage stage) {
        this.stage = stage == null ? null : stage.getId();
    }

    public NotificationStage getStage() {
        return stage == null ? null : NotificationStage.fromId(stage);
    }


    @Transient
    public Set<Object> getObjects() {
        return objects;
    }

    public void setObjects(Set<Object> objects) {
        this.objects = objects;
    }

    public void setTarget(User target) {
        this.target = target;
    }

    public User getTarget() {
        return target;
    }

    public void setMailing(Mailing mailing) {
        this.mailing = mailing;
    }

    public Mailing getMailing() {
        return mailing;
    }


    public TemplateWrapper getTemplate() {
        return template;
    }

    public void setTemplate(TemplateWrapper template) {
        this.template = template;
    }
}