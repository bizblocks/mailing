package com.lokoproject.mailing.service;

import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.Metadata;
import com.lokoproject.mailing.entity.Mailing;
import com.lokoproject.mailing.utils.EntityUtil;
import org.apache.commons.lang.BooleanUtils;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@Service(MailingService.NAME)
@SuppressWarnings("unchecked")
public class MailingServiceBean implements MailingService {

    @Inject
    private DaoService daoService;

    @Inject
    private Metadata metadata;

    @Inject
    private DataManager dataManager;

    private Map<UUID,Map<String,Mailing>> personalizedMailingForEntityMap;
    private Map<String,Map<String,Mailing>> personalizedMailingForTypeMap;

    private Map<String,Mailing> mailingMap;

    @Override
    public Mailing getPersonalizedMailing(Mailing mailing, UUID targetEntityUuid, String targetEntityType) {
        init();
        Mailing personalizedMailing=getPersonalizedMailingFromMap(mailing,targetEntityUuid,targetEntityType);

        if(personalizedMailing==null){
            personalizedMailing=daoService.getPersonalizedMailing(mailing,targetEntityUuid,targetEntityType);
            if(personalizedMailing!=null) putPersonalisedMailingToMap(targetEntityUuid,targetEntityType,personalizedMailing);
        }

        if(personalizedMailing==null){
            return mailing;
        }
        else{
            if(BooleanUtils.isTrue(personalizedMailing.getUseDefaultMailing())){
                return mailing;
            }

            //если свойство не персонализировано, то проставляем его из оригинала рассылки
            for(java.lang.reflect.Field field:Mailing.class.getDeclaredFields()){
                try {
                    if (!java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                        if ((field.getAnnotation(Mailing.Personalized.class) == null) && ((field.getAnnotation(Mailing.PersonalizedOnly.class) == null))) {
                            personalizedMailing.setValue(field.getName(), mailing.getValue(field.getName()));
                        }
                    }
                }
                catch (Exception ignored){}
            }
            return personalizedMailing;
        }
    }

    private Mailing getPersonalizedMailingFromMap(Mailing mailing, UUID targetEntityUuid, String targetEntityType){
        Mailing personalizedMailing;
        try {
            personalizedMailing = personalizedMailingForEntityMap.get(targetEntityUuid).get(mailing.getStringId());
        }
        catch (Exception e){
            try {
                personalizedMailing = personalizedMailingForTypeMap.get(targetEntityType).get(mailing.getStringId());
            }
            catch (Exception ee){
                return null;
            }
        }
        return personalizedMailing;
    }

    @Override
    public Mailing getMailingById(String id){
        init();
        return mailingMap.get(id);
    }

    @Override
    public Mailing createPersonalSettings(UUID entityId, String entityType, Mailing originMailing){
        init();
        Mailing result=getPersonalizedMailingFromMap(originMailing,entityId,entityType);

        if(result==null){
            Mailing personalizedMailing=metadata.create(Mailing.class);
            EntityUtil.createEntityCopy(originMailing,personalizedMailing,false,Arrays.asList("id"));
            personalizedMailing.setOriginMailing(originMailing);
            personalizedMailing.setEntityIdForPersonalSettings(entityId);
            personalizedMailing.setEntityTypeForPersonalSettings(entityType);
            result=dataManager.commit(personalizedMailing,"mailing-full"); //в карту добавляться будет в слушателе

            putPersonalisedMailingToMap(entityId,entityType,result);
        }

        return result;

    }

    @Override
    public void updateMailing(Mailing mailing){
        init();
        if((mailing.getEntityIdForPersonalSettings()==null)&&(mailing.getEntityTypeForPersonalSettings()==null)){
            mailingMap.put(mailing.getStringId(),mailing);
        }
        else{
            putPersonalisedMailingToMap(mailing.getEntityIdForPersonalSettings(),mailing.getEntityTypeForPersonalSettings(),mailing);
        }
    }

    @Override
    public void onRemoveMailing(Mailing mailing) {
        if((mailing.getEntityIdForPersonalSettings()==null)&&(mailing.getEntityTypeForPersonalSettings()==null)){
            mailingMap.remove(mailing.getStringId(),mailing);
        }
        else{
            putPersonalisedMailingToMap(mailing.getEntityIdForPersonalSettings(),mailing.getEntityTypeForPersonalSettings(),null);
        }
    }

    private void putPersonalisedMailingToMap(UUID entityId, String entityType, Mailing mailing){
        if(entityId!=null){
            personalizedMailingForEntityMap.putIfAbsent(entityId,new ConcurrentHashMap<>());
            personalizedMailingForEntityMap.get(entityId).put(mailing.getStringId(),mailing);
        }
        else if(entityType!=null){
            personalizedMailingForTypeMap.putIfAbsent(entityType,new ConcurrentHashMap<>());
            personalizedMailingForTypeMap.get(entityType).put(mailing.getStringId(),mailing);
        }
    }

    private void init(){
        if(personalizedMailingForEntityMap !=null) return;

        Collection<Mailing> allMailings=(Collection)daoService.getAllEntities("mailing$Mailing");
        if(allMailings==null) return;

        personalizedMailingForEntityMap =new ConcurrentHashMap<>();
        personalizedMailingForTypeMap=new ConcurrentHashMap<>();

        mailingMap=new ConcurrentHashMap<>();

        allMailings.forEach(mailing -> {
            if((mailing.getEntityIdForPersonalSettings()==null)&&(mailing.getEntityTypeForPersonalSettings()==null)){
                mailingMap.put(mailing.getStringId(),mailing);
            }
            else{
                putPersonalisedMailingToMap(mailing.getEntityIdForPersonalSettings(),mailing.getEntityTypeForPersonalSettings(),mailing);
            }
        });
    }



}