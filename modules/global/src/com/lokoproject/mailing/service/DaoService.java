package com.lokoproject.mailing.service;


import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.cuba.core.global.View;
import com.haulmont.cuba.security.entity.User;
import com.lokoproject.mailing.entity.Mailing;
import com.lokoproject.mailing.entity.MailingIdentifier;
import com.lokoproject.mailing.entity.Notification;
import com.lokoproject.mailing.entity.WayToGetMailingIdentifier;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface DaoService {
    String NAME = "mailing_DaoService";

    User getUserByLogin(String login);

    StandardEntity getEntity(String entityType, String entityUUID);

    UUID getEntityIdByFieldValue(String entityType, String fieldName, String fieldValue);

    MailingIdentifier getIdentifier(String keyString, String identifierName);

    Notification getNotificationById(String id);

    Mailing getPersonalizedMailing(Mailing mailing, UUID targetEntityUuid, String targetEntityType);

    List<Mailing> getAllPersonalizedMailing();

    Collection<Entity> getAllEntities(String metaClassName);

    StandardEntity getEntity(String entityType, String entityId, View view);

    List<String> getFieldFromTable(String tableName, String columnName, String searchColumnName, String searchColumnValue);

    List<String> getFieldFromTable(String tableName, String columnName, Map<String, String> searchConditionsMap);

    void setFieldToTableAndCreateIfNotExist(String tableName, String columnName, String columnValue, String searchColumnName, String searchColumnValue);

    void setFieldsToTableAndCreateIfNotExist(String tableName,
                                             List<Map<String, String>> valueMapList,
                                             Map<String, String> searchConditionsMap,
                                             List<String> propertiesToCheckExistingOfEachEntry);

    void setFieldsToTableAndCreateIfNotExist(String tableName, Map<String, String> valueMap, Map<String, String> searchConditionsMap);

    List<Map<String, String>> getAllFieldsFromTable(String tableName, String searchColumnName, String searchColumnValue);

    List<Map<String, String>> getAllFieldsFromTable(String tableName, Map<String, String> searchConditionsMap);

    WayToGetMailingIdentifier getConcreteWayToGetMailingIdentifier(String entityType, String entityId, String chanelName);

    WayToGetMailingIdentifier getGeneralWayToGetMailingIdentifier(String entityType, String chanelName);

    WayToGetMailingIdentifier getConcreteWayToGetMailingIdentifierByChannelIdentifier(String identifier, String channelName);
}