package com.lokoproject.mailing.service;


import com.haulmont.bali.util.ParamsMap;
import com.haulmont.chile.core.model.MetaProperty;
import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.View;
import com.lokoproject.mailing.dto.EntityDescription;
import com.lokoproject.mailing.entity.MailingIdentifier;
import com.lokoproject.mailing.entity.WayToGetMailingIdentifier;
import com.lokoproject.mailing.notification.event.AbstractNotificationEvent;
import com.lokoproject.mailing.utils.EntityUtil;
import com.lokoproject.mailing.utils.ReflectionHelper;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service(IdentifierService.NAME)
public class IdentifierServiceBean implements IdentifierService {

    @Inject
    private DaoService daoService;

    @Inject
    private Metadata metadata;

    @Inject
    private DataManager dataManager;

    @Override
    public String getIdentifier(Object key, String identifierName){
        if(key instanceof StandardEntity){
            if(entityContainsIdentifierNameInItsField((StandardEntity) key,identifierName)){
                return ((StandardEntity)key).getValue(identifierName);
            }
        }
        MailingIdentifier identifier=daoService.getIdentifier(getKeyString(key),identifierName);
        if(identifier==null) return null;
        return identifier.getIdentifierValue();
    }

//    @Override
//    public EntityDescription getEntityDescriptionByIdentifier(String identifier, String chanelName){
//
//    }
//
//    @Override
//    public Collection<String> getAvailableLocalizedEntityTypes(){
//
//    }
//
//    @Override
//    public String getLocalizedExternalIdNameByEntityType(String entityType){
//
//    }

    @Override
    public String getIdentifier(String entityType, String entityId, String chanelName){
        WayToGetMailingIdentifier wayToGetMailingIdentifier=daoService.getConcreteWayToGetMailingIdentifier(entityType,entityId,chanelName);
        if(wayToGetMailingIdentifier==null){
            wayToGetMailingIdentifier=daoService.getGeneralWayToGetMailingIdentifier(entityType,chanelName);
            if(wayToGetMailingIdentifier==null){
                StandardEntity entity=daoService.getEntity(entityType,entityId);
                try{
                    AbstractNotificationEvent notificationEvent=ReflectionHelper.getNotificationEvent(chanelName);
                    return entity.getValue(notificationEvent.getDefaultIdentifierName());
                }
                catch (Exception e){
                    return null;
                }
            }
            else{
                try {
                    View view = EntityUtil.createView(entityType, wayToGetMailingIdentifier.getWayToGetIdentiferFromEntityFields());
                    StandardEntity entity = daoService.getEntity(entityType, entityId, view);
                    return entity.getValueEx(wayToGetMailingIdentifier.getWayToGetIdentiferFromEntityFields());
                }
                catch (Exception ignored){
                    // TODO: 09.05.2019 обработка ошибки -сообщение админу
                    return null;
                }
            }
        }
        else {
            return wayToGetMailingIdentifier.getMailingIdentifier();
        }

    }

    @Override
    public void setIdentifier(Object key, String identifierName, String identifierValue){
        MailingIdentifier identifier=daoService.getIdentifier(getKeyString(key),identifierName);

        if(identifier==null){
            if(entityContainsIdentifierNameInItsField((StandardEntity) key,identifierName)){
                return;
            }
            identifier=metadata.create(MailingIdentifier.class);
            identifier.setObjectId(getKeyString(key));

        }

        identifier.setIdentifierName(identifierName);
        identifier.setIdentifierValue(identifierValue);

        dataManager.commit(identifier);
    }

    @Override
    public StandardEntity getEntityByIdentifier(String identifier, String channelName) {
        WayToGetMailingIdentifier mailingIdentifier=daoService.getConcreteWayToGetMailingIdentifierByChannelIdentifier(identifier,channelName);

        if(mailingIdentifier==null) return null;
        if((!StringUtils.isEmpty(mailingIdentifier.getEntityType()))&&((!StringUtils.isEmpty(mailingIdentifier.getEntityId())))){
            return daoService.getEntity(mailingIdentifier.getEntityType(),mailingIdentifier.getEntityId());
        }
        return null;
    }

    @Override
    public Set<String> getAvailableEntityTypesForChanel(String telegram) {

        Collection<WayToGetMailingIdentifier> allTelegramIdentifiers=(Collection)daoService.getAllEntities(metadata.getSession().getClass(WayToGetMailingIdentifier.class).getName());
        Set<String> result=new HashSet<>();
        allTelegramIdentifiers.forEach(identifier->{
            if(identifier.getEntityType()!=null){
                result.add(identifier.getEntityType());
            }
        });
        return result;
    }

    @Override
    public WayToGetMailingIdentifier createIdentifier(StandardEntity entity, String identifier, String channelName) {
        WayToGetMailingIdentifier result=metadata.create(WayToGetMailingIdentifier.class);
        result.setEntityId(entity.getId().toString());
        result.setEntityType(entity.getMetaClass().getName());
        result.setIsGeneral(false);
        result.setMailingIdentifier(identifier);
        result.setNotificationChanel(channelName);

        try{
            String fieldName=getFieldNameByChannel(channelName);
            if(entityContainsIdentifierNameInItsField(entity,fieldName)){
                entity.setValue(fieldName,identifier);
                dataManager.commit(entity);
            }
        }
        catch (Exception ignored){}

        return dataManager.commit(result);
    }

    @Override
    public StandardEntity getEntityByFieldMarkedAsExternalIdentifier(String entityType, String extIdentifierValue, String channelName) {

        try {
            WayToGetMailingIdentifier wayToGetMailingIdentifier = daoService.getGeneralWayToGetMailingIdentifier(entityType, channelName);
            UUID entityId = daoService.getEntityIdByFieldValue(entityType, wayToGetMailingIdentifier.getFieldUsedAsExternalId(), extIdentifierValue);
            return daoService.getEntity(entityType, entityId.toString());
        }
        catch (Exception e){
            return null;
        }

    }

    private boolean entityContainsIdentifierNameInItsField(StandardEntity entity,String identifierName){
        Collection<MetaProperty> properties = entity.getMetaClass().getProperties();

        for(MetaProperty property:properties){
            if(property.getName().equalsIgnoreCase(identifierName)) return true;
        }
        return false;
    }

    private String getFieldNameByChannel(String channelName){
        return ReflectionHelper.getNotificationEvent(channelName).getDefaultIdentifierName();
    }
    
    private String getKeyString(Object key){

        String keyString;
        if(key instanceof StandardEntity){
            keyString=((StandardEntity)key).getId().toString();
            
        }
        else{
            keyString=key.toString();
        }
        return keyString;
    }

}