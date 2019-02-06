package com.lokoproject.mailing.entity;

import javax.persistence.*;

import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.cuba.security.entity.User;

import java.util.HashSet;
import java.util.Set;
import java.util.Date;

@Table(name = "MAILING_NOTIFICATION")
@Entity(name = "mailing$Notification")
public class Notification extends StandardEntity {
    private static final long serialVersionUID = -3219109399113325120L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TARGET_ID")
    protected User target;

    @Temporal(TemporalType.DATE)
    @Column(name = "SEND_DATE")
    protected Date sendDate;

    @Column(name = "STAGE")
    protected Integer stage;

    @Lob
    @Column(name = "TEMPLATE")
    protected String template;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MAILING_ID")
    protected Mailing mailing;

    @Transient
    private Set<Object> objects=new HashSet<>();

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

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getTemplate() {
        return template;
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


}