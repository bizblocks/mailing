package com.lokoproject.mailing.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.validation.constraints.NotNull;
import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.chile.core.annotations.NamePattern;

@NamePattern("%s for %s|identifierName,objectId")
@Table(name = "MAILING_MAILING_IDENTIFIER")
@Entity(name = "mailing$MailingIdentifier")
public class MailingIdentifier extends StandardEntity {
    private static final long serialVersionUID = -2315998189892765849L;

    @Column(name = "OBJECT_ID", unique = true)
    protected String objectId;

    @NotNull
    @Column(name = "IDENTIFIER_NAME", nullable = false)
    protected String identifierName;

    @NotNull
    @Column(name = "IDENTIFIER_VALUE", nullable = false)
    protected String identifierValue;

    public void setIdentifierName(String identifierName) {
        this.identifierName = identifierName;
    }

    public String getIdentifierName() {
        return identifierName;
    }

    public void setIdentifierValue(String identifierValue) {
        this.identifierValue = identifierValue;
    }

    public String getIdentifierValue() {
        return identifierValue;
    }


    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getObjectId() {
        return objectId;
    }


}