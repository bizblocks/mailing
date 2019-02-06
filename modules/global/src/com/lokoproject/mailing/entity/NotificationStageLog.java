package com.lokoproject.mailing.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.cuba.core.entity.annotation.OnDeleteInverse;
import com.haulmont.cuba.core.global.DeletePolicy;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Table(name = "MAILING_NOTIFICATION_STAGE_LOG")
@Entity(name = "mailing$NotificationStageLog")
public class NotificationStageLog extends StandardEntity {
    private static final long serialVersionUID = 7723242202480255008L;

    @Temporal(TemporalType.DATE)
    @Column(name = "DATE_")
    protected Date date;

    @Column(name = "STAGE")
    protected Integer stage;

    @OnDeleteInverse(DeletePolicy.CASCADE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "NOTIFICATION_ID")
    protected Notification notification;

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getDate() {
        return date;
    }

    public void setStage(NotificationStage stage) {
        this.stage = stage == null ? null : stage.getId();
    }

    public NotificationStage getStage() {
        return stage == null ? null : NotificationStage.fromId(stage);
    }

    public void setNotification(Notification notification) {
        this.notification = notification;
    }

    public Notification getNotification() {
        return notification;
    }


}