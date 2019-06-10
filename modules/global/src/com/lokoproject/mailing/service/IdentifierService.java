package com.lokoproject.mailing.service;


import com.haulmont.cuba.core.entity.StandardEntity;
import com.lokoproject.mailing.entity.WayToGetMailingIdentifier;

import java.util.Set;

public interface IdentifierService {
    String NAME = "mailing_IdentifierService";

    String getIdentifier(Object key, String identifierName);

    String getIdentifier(String entityType, String entityId, String chanelName);

    void setIdentifier(Object key, String identifierName, String identifierValue);

    StandardEntity getEntityByIdentifier(String identifier, String identifierName);

    Set<String> getAvailableEntityTypesForChanel(String telegram);

    WayToGetMailingIdentifier createIdentifier(StandardEntity entity, String identifier, String channelName);

    StandardEntity getEntityByFieldMarkedAsExternalIdentifier(String entityType, String extIdentifierValue, String channelName);
}