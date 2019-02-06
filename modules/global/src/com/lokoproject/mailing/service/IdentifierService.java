package com.lokoproject.mailing.service;


public interface IdentifierService {
    String NAME = "mailing_IdentifierService";

    String getIdentifier(Object key, String identifierName);

    void setIdentifier(Object key, String identifierName, String identifierValue);
}