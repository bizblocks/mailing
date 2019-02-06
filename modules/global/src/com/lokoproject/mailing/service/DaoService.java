package com.lokoproject.mailing.service;


import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.cuba.security.entity.User;
import com.lokoproject.mailing.entity.MailingIdentifier;

public interface DaoService {
    String NAME = "mailing_DaoService";

    User getUserByLogin(String login);

    StandardEntity getEntity(String entityType, String entityUUID);

    MailingIdentifier getIdentifier(String keyString, String identifierName);
}