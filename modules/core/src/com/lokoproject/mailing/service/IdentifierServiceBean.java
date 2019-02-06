package com.lokoproject.mailing.service;


import com.haulmont.chile.core.model.MetaProperty;
import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.Metadata;
import com.lokoproject.mailing.entity.MailingIdentifier;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Collection;

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

    private boolean entityContainsIdentifierNameInItsField(StandardEntity entity,String identifierName){
        Collection<MetaProperty> properties = entity.getMetaClass().getProperties();

        for(MetaProperty property:properties){
            if(property.getName().equalsIgnoreCase(identifierName)) return true;
        }
        return false;
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