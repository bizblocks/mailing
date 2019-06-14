package com.lokoproject.mailing.service;

import com.haulmont.bali.util.ParamsMap;
import com.haulmont.chile.core.annotations.MetaClass;
import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Query;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.View;
import com.haulmont.cuba.security.entity.User;
import com.lokoproject.mailing.entity.Mailing;
import com.lokoproject.mailing.entity.MailingIdentifier;
import com.lokoproject.mailing.entity.Notification;
import com.lokoproject.mailing.entity.WayToGetMailingIdentifier;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.persistence.Table;
import java.util.*;

@Service(DaoService.NAME)
public class DaoServiceBean implements DaoService {

    @Inject
    private DataManager dataManager;

    @Inject
    private Metadata metadata;

    @Inject
    private com.haulmont.cuba.core.Persistence persistence;

    @Override
    public User getUserByLogin(String login){
        LoadContext loadContext = LoadContext.create(User.class)
                .setQuery(LoadContext.createQuery("select e from sec$User e where e.login=:loginItem")
                        .setParameter("loginItem",login ))
                ;
        User result=(User) dataManager.load(loadContext);
        return result;
    }

    @Override
    public StandardEntity getEntity(String entityType, String entityUUID) {
        if((entityType==null)||(entityUUID==null))return null;

        Transaction tx = persistence.createTransaction();
        Object en;
        try {
            EntityManager em = persistence.getEntityManager();
            en = em.createQuery(
                    "SELECT e FROM "  + entityType + " e where e.id=:id")
                    .setParameter("id", UUID.fromString(entityUUID))
                    .getSingleResult();


            tx.commit();
        }
        finally {
            tx.end();
        }

        return (StandardEntity)en;
    }

    @Override
    public UUID getEntityIdByFieldValue(String entityType, String fieldName, String fieldValue){
        try {
            com.haulmont.chile.core.model.MetaClass metaClass=metadata.getSession().getClass(entityType);

            Table tableAnnotation=null;
            Class currentClass=metaClass.getJavaClass();
            tableAnnotation=(Table) currentClass.getAnnotation(Table.class);
            while(tableAnnotation==null){
                currentClass=currentClass.getSuperclass();
                tableAnnotation=(Table) currentClass.getAnnotation(Table.class);
            }
            String tableName = tableAnnotation.name();
            List idList = getFieldFromTable(tableName, "id", fieldName, fieldValue);
            if (idList.size() == 1) {
                return (UUID) idList.get(0);
            }
        }
        catch (Exception ignored){
            ignored.printStackTrace();
        }

        return null;
    }

    @Override
    public MailingIdentifier getIdentifier(String keyString, String identifierName) {
        LoadContext loadContext = LoadContext.create(MailingIdentifier.class)
                .setQuery(LoadContext.createQuery("select e from mailing$MailingIdentifier e where " +
                        "e.objectId=:idItem " +
                        "and e.identifierName=:nameItem")
                        .setParameter("idItem",keyString )
                        .setParameter("nameItem",identifierName))
                ;
        MailingIdentifier result=(MailingIdentifier) dataManager.load(loadContext);
        return result;
    }

    @Override
    public Notification getNotificationById(String id) {
        LoadContext<Notification> loadContext = LoadContext.create(Notification.class)
                .setQuery(LoadContext.createQuery("select  n from mailing$Notification n where n.id = :idItem")
                        .setParameter("idItem",UUID.fromString(id)))
                .setView("notification-full");
        return dataManager.load(loadContext);
    }

    @Override
    public Mailing getPersonalizedMailing(Mailing mailing, UUID targetEntityUuid, String targetEntityType) {
        LoadContext loadContext = LoadContext.create(Mailing.class)
                .setQuery(LoadContext.createQuery("select e from mailing$Mailing e where " +
                        " e.originMailing.id=:mailingItem " +
                        " and e.entityTypeForPersonalSettings=:typeItem" +
                        " and e.entityIdForPersonalSettings=:idItem")
                        .setParameter("idItem",targetEntityUuid )
                        .setParameter("typeItem",targetEntityType)
                        .setParameter("mailingItem",mailing))
                .setView("mailing-full");
        return (Mailing) dataManager.load(loadContext);

    }

    @Override
    public List<Mailing> getAllPersonalizedMailing() {
        LoadContext loadContext = LoadContext.create(Mailing.class)
                .setQuery(LoadContext.createQuery("select e from mailing$Mailing e where " +
                        " e.entityTypeForPersonalSettings is not null" +
                        " or e.entityIdForPersonalSettings is not null")
                        )
                .setView("mailing-full");
        return  dataManager.loadList(loadContext);

    }

    @Override
    public Collection<Entity> getAllEntities(String metaClassName){

        String viewName=metaClassName.substring(metaClassName.indexOf("$")+1,metaClassName.indexOf("$")+2).toLowerCase()+metaClassName.substring(metaClassName.indexOf("$")+2);

        LoadContext loadContext = LoadContext.create(metadata.getClassNN(metaClassName).getJavaClass())
                .setQuery(LoadContext.createQuery("select  e from "+metaClassName+" e ")
                      )
                .setView(viewName+"-full");


        return dataManager.loadList(loadContext);
    }

    @Override
    public WayToGetMailingIdentifier getGeneralWayToGetMailingIdentifier(String entityType, String chanelName) {
        LoadContext loadContext = LoadContext.create(WayToGetMailingIdentifier.class)
                .setQuery(LoadContext.createQuery("select e from mailing$WayToGetMailingIdentifier e where " +
                        "e.notificationChanel=:chanelItem " +
                        "and e.entityType=:typeItem and e.isGeneral=true" )
                        .setParameter("chanelItem",chanelName )
                        .setParameter("typeItem",entityType))
                .setView("wayToGetMailingIdentifier-full");

        WayToGetMailingIdentifier result= (WayToGetMailingIdentifier) dataManager.load(loadContext);
        return result;
    }

    @Override
    public WayToGetMailingIdentifier getConcreteWayToGetMailingIdentifierByChannelIdentifier(String identifier, String channelName) {
        LoadContext loadContext = LoadContext.create(WayToGetMailingIdentifier.class)
                .setQuery(LoadContext.createQuery("select e from mailing$WayToGetMailingIdentifier e where " +
                        "e.notificationChanel=:chanelItem " +
                        "and e.mailingIdentifier=:identifierItem and e.isGeneral=false " )
                        .setParameter("chanelItem",channelName )
                        .setParameter("identifierItem",identifier)
                        )
                .setView("wayToGetMailingIdentifier-full");

        WayToGetMailingIdentifier result= (WayToGetMailingIdentifier) dataManager.load(loadContext);
        return result;
    }


    @Override
    public WayToGetMailingIdentifier getConcreteWayToGetMailingIdentifier(String entityType, String entityId, String chanelName) {
        LoadContext loadContext = LoadContext.create(WayToGetMailingIdentifier.class)
                .setQuery(LoadContext.createQuery("select e from mailing$WayToGetMailingIdentifier e where " +
                        "e.notificationChanel=:chanelItem " +
                        "and e.entityType=:typeItem and e.isGeneral=false and e.entityId=:idItem" )
                        .setParameter("chanelItem",chanelName )
                        .setParameter("typeItem",entityType)
                        .setParameter("idItem",entityId))
                .setView("wayToGetMailingIdentifier-full");

        WayToGetMailingIdentifier result= (WayToGetMailingIdentifier) dataManager.load(loadContext);
        return result;
    }

    @Override
    public StandardEntity getEntity(String entityType, String entityId, View view) {
        return null;
    }

    @Override
    public List<String> getFieldFromTable(String tableName, String columnName, String searchColumnName, String searchColumnValue){
        return getFieldFromTable(tableName,columnName,(Map)ParamsMap.of(searchColumnName,searchColumnValue));
    }

    @Override
    public List<String> getFieldFromTable(String tableName, String columnName, Map<String, String> searchConditionsMap) {
        final Transaction transaction = persistence.createTransaction();
        try {
            Query query = persistence.getEntityManager().createNativeQuery(
                    "select " + columnName + " from " + tableName + " where " + makeSearchCondition(searchConditionsMap,"and"));
            return  query.getResultList();
        }
        catch (Exception e){
            return null;
        }
        finally {
            transaction.end();
        }
    }



    @Override
    public void setFieldToTableAndCreateIfNotExist(String tableName, String columnName, String columnValue, String searchColumnName, String searchColumnValue){
        final Transaction transaction = persistence.createTransaction();
        tableName=tableName.toLowerCase();
        columnName=columnName.toLowerCase();
        searchColumnName=searchColumnName.toLowerCase();
        try {

            if (!checkIfTableExists(tableName)) {
                createTable(tableName, Arrays.asList(columnName, searchColumnName));
            }
            else {
                if (!checkIfColumnExists(tableName, columnName)) {
                    addColumn(tableName, columnName);
                }
                if (!checkIfColumnExists(tableName, searchColumnName)) {
                    addColumn(tableName, searchColumnName);
                }
            }

            if (checkIfEntryExists(tableName, searchColumnName, searchColumnValue)) {
                updateTable(tableName, columnName, columnValue, searchColumnName, searchColumnValue);
            } else {
                insertToTable(tableName, (Map) ParamsMap.of(searchColumnName, searchColumnValue, columnName, columnValue));
            }
            transaction.commit();
        }
        finally {
            transaction.end();
        }

    }

    /**
     *
     * @param propertiesToCheckExistingOfEachEntry при одновременной вставке или обновлении нескольких элементов
     *                                             у которых значение в searchConditionsMap одинаковые
     *                                             нужны дополнительные свойства для идентификации
     */
    @Override
    public void setFieldsToTableAndCreateIfNotExist(String tableName,
                                                    List<Map<String, String>> valueMapList,
                                                    Map<String, String> searchConditionsMap,
                                                    List<String> propertiesToCheckExistingOfEachEntry){

        final Transaction transaction = persistence.createTransaction();
        final String finalTableName=tableName.toLowerCase();

        try {
            valueMapList.forEach(valueMap->{
                Set<String> allColumns=new HashSet<>();
                allColumns.addAll(valueMap.keySet());
                allColumns.addAll(searchConditionsMap.keySet());

                if (!checkIfTableExists(finalTableName)) {
                    createTable(finalTableName, new ArrayList<>(allColumns));
                }

                allColumns.forEach(columnName->{
                    if (!checkIfColumnExists(finalTableName, columnName)) {
                        addColumn(finalTableName, columnName);
                    }
                });

                Map<String, String> conditionMapToCheckEntryExisting=new HashMap<String, String>(searchConditionsMap);
                propertiesToCheckExistingOfEachEntry.forEach(property->{
                    conditionMapToCheckEntryExisting.put(property,valueMap.get(property));
                });
                if (checkIfEntryExists(finalTableName,conditionMapToCheckEntryExisting)) {
                    Map<String, String> valueMapToUpdate=new HashMap<String, String>(valueMap);
                    propertiesToCheckExistingOfEachEntry.forEach(valueMapToUpdate::remove);
                    updateTable(finalTableName, valueMapToUpdate,conditionMapToCheckEntryExisting);
                } else {
                    Map<String,String> allValuesMap=new HashMap<>();
                    allValuesMap.putAll(searchConditionsMap);
                    allValuesMap.putAll(valueMap);
                    insertToTable(finalTableName, allValuesMap);
                }

            });
            transaction.commit();
        }
        finally {
            transaction.end();
        }
    }

    @Override
    public void setFieldsToTableAndCreateIfNotExist(String tableName, Map<String, String> valueMap, Map<String, String> searchConditionsMap){
        setFieldsToTableAndCreateIfNotExist(tableName,Collections.singletonList(valueMap),searchConditionsMap,Collections.emptyList());
    }



    @Override
    public List<Map<String, String>> getAllFieldsFromTable(String tableName, String searchColumnName, String searchColumnValue){
        return getAllFieldsFromTable(tableName,(Map)ParamsMap.of(searchColumnName,searchColumnValue));
    }

    @Override
    public List<Map<String, String>> getAllFieldsFromTable(String tableName, Map<String, String> searchConditionsMap){

        final Transaction transaction = persistence.createTransaction();
        List<String> allColumns=getAllColumnsOfTable(tableName);
        String columnString=makeColumnsString(allColumns);
        try {
            Query query = persistence.getEntityManager().createNativeQuery(
                    "select "+columnString+" from " + tableName + " where " + makeSearchCondition(searchConditionsMap,"and"));

            List<Map<String, String>> result=new ArrayList<>();
            query.getResultList().forEach(entry->{
                Map<String,String> entryMap=new HashMap<>();
                Object [] resultStringArr= (Object[]) entry;
                for(int i=0;i<resultStringArr.length;i++){
                    entryMap.put(allColumns.get(i), (String) resultStringArr[i]);
                }
                result.add(entryMap);
            });

            return result;
        }
        catch (Exception e){
            return Collections.emptyList();
        }
        finally {
            transaction.end();
        }
    }




    private List<String> getAllColumnsOfTable(String tableName) {
        try {
            Query query = persistence.getEntityManager().createNativeQuery(
                    "SELECT column_name FROM INFORMATION_SCHEMA.COLUMNS WHERE" +
                            "  TABLE_NAME ='" + tableName+"'");
            List result=query.getResultList();
            if(result!=null){
                return result;
            }
            else {
                return Collections.emptyList();
            }
        }
        catch (Exception e){
            return Collections.emptyList();
        }

    }

    private String makeColumnsString(List<String> allColumns) {
        StringBuilder sb=new StringBuilder();

        int i=0;
        for(String columnName:allColumns){
            sb.append(columnName);
            i++;
            if(i<allColumns.size()) sb.append(",");
        }
        return sb.toString();
    }

    private String makeSearchCondition(Map<String,String> searchConditionsMap,String condition){
        StringBuilder sb=new StringBuilder();

        int i=0;
        for(String columnName:searchConditionsMap.keySet()){
            if(searchConditionsMap.get(columnName)!=null) {
                sb.append(columnName).append(" = '").append(searchConditionsMap.get(columnName)).append("'");
            }
            else{
                sb.append(columnName).append(" is null ");
            }
            if(i<searchConditionsMap.size()-1) {
                sb.append(" ").append(condition).append(" ");
            }
            i++;

        }

        return sb.toString();
    }

    private String makeValueString(Map<String, String> valueMap) {
        StringBuilder sb=new StringBuilder();

        int i=0;
        for(String columnName:valueMap.keySet()){

            if(valueMap.get(columnName)!=null){
                sb.append(columnName).append("='").append(valueMap.get(columnName)).append("'");
            }
            else{
                sb.append(columnName).append("=null");
            }

            if(i<valueMap.size()-1) {
                sb.append(",");
            }
            i++;
        }
        return sb.toString();
    }

    private boolean checkIfEntryExists(String tableName,String searchColumnName, String searchColumnValue){
        return checkIfEntryExists(tableName,(Map)ParamsMap.of(searchColumnName,searchColumnValue));
    }

    private boolean checkIfEntryExists(String tableName,Map<String,String> searchConditionsMap){
        try {
            Query query = persistence.getEntityManager().createNativeQuery(
                    "select exists(SELECT * FROM "+tableName+" WHERE " +makeSearchCondition(searchConditionsMap,"and")+")");
            return (Boolean) query.getSingleResult();
        }
        catch (Exception e){
            return false;
        }
    }

    private boolean checkIfColumnExists(String tableName, String columnName){
        try {
            Query query = persistence.getEntityManager().createNativeQuery(
                    "select exists(SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE" +
                            "  TABLE_NAME ='" + tableName +
                            "' AND COLUMN_NAME ='" + columnName + "')");
            return (Boolean) query.getSingleResult();
        }
        catch (Exception e){
            return false;
        }
    }

    private boolean checkIfTableExists(String tableName){
        try {
            Query query = persistence.getEntityManager().createNativeQuery(
                    "select exists(SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE " +
                            "  TABLE_NAME ='" + tableName +"')");
            return (Boolean) query.getSingleResult();
        }
        catch (Exception e){
            return false;
        }
    }

    private void createTable(String tableName,Collection<String> columnNames){
        StringBuilder sb=new StringBuilder();
        sb.append("CREATE TABLE public.").append(tableName).append(" (");

        int i=0;
        for(String columnName:columnNames){
            sb.append(columnName).append(" ").append("character varying(255)");
            i++;
            if(i<columnNames.size()) sb.append(",");
        }
        sb.append(")");

        Query query = persistence.getEntityManager().createNativeQuery(sb.toString());
        query.executeUpdate();

    }

    private void addColumn(String tableName,String columnName){
        Query query = persistence.getEntityManager().createNativeQuery(
                "alter table "+tableName+"  add column " +columnName+" varchar(255)") ;
        query.executeUpdate();
    }

    private void insertToTable(String tableName,Map<String,String> valueMap){
        StringBuilder sb=new StringBuilder();
        sb.append("insert into ").append(tableName).append(" (");

        StringBuilder columnNameBuilder=new StringBuilder();
        StringBuilder valuesBuilder=new StringBuilder();
        valueMap.forEach((key,value)->{
            columnNameBuilder.append(key).append(",");
            if(value!=null){
                valuesBuilder.append("'").append(value).append("'").append(",");
            }
            else{
                valuesBuilder.append("null,");
            }
        });

        columnNameBuilder.deleteCharAt(columnNameBuilder.length()-1);
        valuesBuilder.deleteCharAt(valuesBuilder.length()-1);

        sb.append(columnNameBuilder.toString()).append(")").append(" values (").append(valuesBuilder.toString()).append("); ");
        Query query = persistence.getEntityManager().createNativeQuery(sb.toString());
        query.executeUpdate();
    }

    private void updateTable(String tableName, String columnName, String columnValue, String searchColumnName, String searchColumnValue){
        Query query = persistence.getEntityManager().createNativeQuery(
                "update "+tableName+"  set  " +columnName+" ='"+columnValue+"' where "+searchColumnName+" = '"+searchColumnValue+"'") ;
        query.executeUpdate();
    }

    private void updateTable(String tableName, Map<String, String> valueMap, Map<String, String> searchConditionsMap) {
        Query query = persistence.getEntityManager().createNativeQuery(
                "update "+tableName+"  set  " +makeValueString(valueMap)+" where "+makeSearchCondition(searchConditionsMap,"and")) ;
        query.executeUpdate();
    }


}