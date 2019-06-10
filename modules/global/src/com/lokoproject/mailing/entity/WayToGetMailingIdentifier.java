package com.lokoproject.mailing.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Column;
import com.haulmont.cuba.core.entity.StandardEntity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Lob;
import javax.persistence.Index;
import javax.persistence.UniqueConstraint;

@Table(name = "MAILING_WAY_TO_GET_MAILING_IDENTIFIER", indexes = {
    @Index(name = "IDX_MAILING_WAY_TO_GET_MAILING_IDENTIFIER", columnList = "NOTIFICATION_CHANEL, MAILING_IDENTIFIER")
}, uniqueConstraints = {
    @UniqueConstraint(name = "IDX_MAILING_WAY_TO_GET_MAILING_IDENTIFIER_UNQ", columnNames = {"ENTITY_ID", "NOTIFICATION_CHANEL"})
})
@Entity(name = "mailing$WayToGetMailingIdentifier")
public class WayToGetMailingIdentifier extends StandardEntity {
    private static final long serialVersionUID = 8129844135014405653L;

    @Column(name = "ENTITY_TYPE")
    protected String entityType;

    @Column(name = "IS_GENERAL")
    protected Boolean isGeneral;

    @Column(name = "ENTITY_ID")
    protected String entityId;

    @Column(name = "WAY_TO_GET_IDENTIFER_FROM_ENTITY_FIELDS")
    protected String wayToGetIdentiferFromEntityFields;

    @Column(name = "NOTIFICATION_CHANEL")
    protected String notificationChanel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MAILING_TO_REQUEST_IDENTIFER_ID")
    protected Mailing mailingToRequestIdentifer;

    @Column(name = "FIELD_USED_AS_EXTERNAL_ID")
    protected String fieldUsedAsExternalId;

    @Lob
    @Column(name = "MAILING_IDENTIFIER")
    protected String mailingIdentifier;

    public void setIsGeneral(Boolean isGeneral) {
        this.isGeneral = isGeneral;
    }

    public Boolean getIsGeneral() {
        return isGeneral;
    }


    public void setMailingIdentifier(String mailingIdentifier) {
        this.mailingIdentifier = mailingIdentifier;
    }

    public String getMailingIdentifier() {
        return mailingIdentifier;
    }


    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getEntityId() {
        return entityId;
    }


    public void setFieldUsedAsExternalId(String fieldUsedAsExternalId) {
        this.fieldUsedAsExternalId = fieldUsedAsExternalId;
    }

    public String getFieldUsedAsExternalId() {
        return fieldUsedAsExternalId;
    }



    public void setNotificationChanel(String notificationChanel) {
        this.notificationChanel = notificationChanel;
    }

    public String getNotificationChanel() {
        return notificationChanel;
    }




    public void setMailingToRequestIdentifer(Mailing mailingToRequestIdentifer) {
        this.mailingToRequestIdentifer = mailingToRequestIdentifer;
    }

    public Mailing getMailingToRequestIdentifer() {
        return mailingToRequestIdentifer;
    }


    public void setWayToGetIdentiferFromEntityFields(String wayToGetIdentiferFromEntityFields) {
        this.wayToGetIdentiferFromEntityFields = wayToGetIdentiferFromEntityFields;
    }

    public String getWayToGetIdentiferFromEntityFields() {
        return wayToGetIdentiferFromEntityFields;
    }


    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getEntityType() {
        return entityType;
    }


}