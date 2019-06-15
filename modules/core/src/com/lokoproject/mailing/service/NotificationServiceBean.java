package com.lokoproject.mailing.service;

import com.haulmont.bali.util.ParamsMap;
import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.cuba.core.entity.contracts.Id;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.security.app.Authenticated;
import com.haulmont.cuba.security.entity.User;
import com.lokoproject.mailing.conditions.ConditionException;
import com.lokoproject.mailing.dto.ResultOfAddingNotification;
import com.lokoproject.mailing.entity.NotificationStage;
import com.lokoproject.mailing.entity.NotificationStageLog;
import com.lokoproject.mailing.notification.event.AbstractNotificationEvent;
import com.lokoproject.mailing.entity.Mailing;
import com.lokoproject.mailing.entity.Notification;
import com.lokoproject.mailing.notification.template.TemplateBuilder;
import com.lokoproject.mailing.notification.template.TemplateWrapper;
import com.lokoproject.mailing.utils.EntityUtil;
import com.lokoproject.mailing.utils.ReflectionHelper;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;


import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service(NotificationService.NAME)
public class NotificationServiceBean implements NotificationService {

    @Inject
    private DataManager dataManager;

    @Inject
    private TimeSource timeSource;

    @Inject
    private Metadata metadata;

    @Inject
    private Events events;

    @Inject
    private DaoService daoService;

    @Inject
    private MailingService mailingService;

    @Inject
    protected Scripting scripting;


    private Map<Mailing,Map<UUID,Notification>> mailingMap=new ConcurrentHashMap<>();
    private Map<Mailing,Map<UUID,Date>> lastSendDateMap=new ConcurrentHashMap<>();
    private Map<String,Notification> actualNotificationMap=new ConcurrentHashMap<>();
    private Collection<Mailing> allMailings;

    @Override
    public void updateMailing(Mailing mailing){
        if(getAllMailings()==null) return;
        if(mailing.getEntityTypeForPersonalSettings()!=null) return;

        getAllMailings().remove(mailing);
        getAllMailings().add(mailing);
    }

    @Override
    public void onRemoveMailing(Mailing mailing){
        if(getAllMailings()==null) return;

        if(mailing.getEntityTypeForPersonalSettings()==null)getAllMailings().remove(mailing);
        mailingService.onRemoveMailing(mailing);
    }

    @Override
    public void sendSimpleNotification(StandardEntity target, String content, String header, String channelName){
        Notification notification=createNotification(target,null,null);
        TemplateBuilder.MainTemplateBuilder builder=TemplateBuilder.createBuilder(header,content,"smile");
        notification.setTemplate((TemplateWrapper) builder.build());
        dispatchNotification(notification,Arrays.asList(ReflectionHelper.getNotificationEvent(channelName)));
    }

    @Override
    public ResultOfAddingNotification addNotification(Object object){
        return addNotification(object,false);
    }

    @Override
    public ResultOfAddingNotification addNotification(Mailing mailing, Object object){
        return addNotification(mailing, object,false);
    }

    @Override
    public ResultOfAddingNotification addNotification(Object object, Boolean onlyCheck){
        ResultOfAddingNotification result=new ResultOfAddingNotification();
        Collection<Mailing> mailings=getAllMailings();
        mailings.forEach(mailing->{
            result.joinResults(addNotification(mailing,object,onlyCheck));
        });
        return result;
    }

    @Override
    public ResultOfAddingNotification addNotification(Mailing mailing, Object object, Boolean onlyCheck){
        ResultOfAddingNotification result=new ResultOfAddingNotification();
        if(mailing.getActivated()){
            if(isAcceptableForObject(mailing,object)){
                getUsersToNotify(mailing,object).forEach(entity->{
                    if(!onlyCheck)addObjectToNotification(entity,mailing,object);
                    result.addMailingTarget(mailing,entity);
                });
            }
        }
        return result;
    }

    @Override
    public void processMailings(){
        List<Notification> noticeToRemove=new ArrayList<>();
        mailingMap.forEach((mailing,userNoticeMap)->{
            userNoticeMap.forEach((user,notification)->{
                if(canSendNow(notification,timeSource.currentTimestamp())){
                    dispatchNotification(notification);
                    if(canRemoveAfterNotificationDone(notification)) noticeToRemove.add(notification);
                }
            });
        });

        noticeToRemove.forEach(this::removeNotification);
    }

    private boolean canRemoveAfterNotificationDone(Notification notification) {
        return true;
    }

    private boolean canSendNow(Notification notification, Date now) {
        Mailing mailing= getMailingOfNotification(notification);
        assert mailing != null;
        if((mailing.getConsolidationConditionJson()==null)&&(mailing.getConsolidationGroovy()==null)) return true;

        Date lastSendDate=null;
        lastSendDateMap.putIfAbsent( getMailingOfNotification(notification),new HashMap<>());
        Map<UUID,Date> userDateMap=lastSendDateMap.get( getMailingOfNotification(notification));
        assert (userDateMap!=null);

        lastSendDate=userDateMap.get(notification.getTargetEntityUuid());
        if(lastSendDate==null){
            lastSendDate=now;   //чтобы при запуске системы сразу накапливаемые уведомления не отправлялись
            userDateMap.put(notification.getTargetEntityUuid(),now);
        }



        if(mailing.getConsolidationConditionJson()!=null){

            try {
                return mailing.getConsolidationCondition().check(
                        ParamsMap.of("lastSendDate",lastSendDate
                                ,"now",now
                                ,"objectsValue",notification.getObjects().size()));
            } catch (ConditionException e) {
                return false;
            }
        }
        else{
            if((mailing.getConsolidationGroovy()==null)||(StringUtils.isEmpty(mailing.getConsolidationGroovy().getScript()))) return true;

            try {
                Map<String, Object> binding = new HashMap<>();
                binding.put("objectsValue", notification.getObjects());
                binding.put("lastSendDate", lastSendDate);
                binding.put("now", now);

                return (Boolean) scripting.evaluateGroovy(mailing.getConsolidationGroovy().getScript(), binding);

            } catch (Exception e) {
                return false;
            }
        }
    }

    @Override
    public Collection<Mailing> loadAllMailings() {
        LoadContext<Mailing> loadContext = LoadContext.create(Mailing.class)
                .setQuery(LoadContext.createQuery("select m from mailing$Mailing m where m.entityTypeForPersonalSettings is null") )
                .setView("mailing-full");

        allMailings= dataManager.loadList(loadContext);
        return new CopyOnWriteArrayList<>(allMailings);
    }

    @Override
    @Authenticated
    public Notification updateNotificationStage(Notification notification, NotificationStage stage){

        if(notification.getStage().getId()>=stage.getId()) return notification;

        notification=dataManager.reload(notification,"_local");
        notification.setStage(stage);
        notification=dataManager.commit(notification,"notification-full");
        makeRecordOfNotificationStateChange(notification,stage);
        if(NotificationStage.READ.getId()>stage.getId()) {
            actualNotificationMap.put(notification.getId().toString(),notification);
        }
        else{
            actualNotificationMap.remove(notification.getId().toString());
        }

        return notification;
    }

    @Override
    @Authenticated
    public void confirmNotificationReceipt(String notificationId){
        Notification notification=dataManager.load(Id.of(UUID.fromString(notificationId),Notification.class)).one();
        notification.setStage(NotificationStage.READ);
        notification=dataManager.commit(notification,"notification-full");
        makeRecordOfNotificationStateChange(notification,NotificationStage.READ);
    }

    private void makeRecordOfNotificationStateChange(Notification notification, NotificationStage stage){
        NotificationStageLog stageLog=metadata.create(NotificationStageLog.class);
        stageLog.setNotification(notification);
        stageLog.setStage(stage);
        stageLog.setDate(timeSource.currentTimestamp());
        dataManager.commit(stageLog);
    }



    private void addObjectToNotification(StandardEntity entity, Mailing mailing, Object object) {
        Map<UUID,Notification> userNotificationMap=mailingMap.get(mailing);
        if(userNotificationMap==null){
            userNotificationMap=new ConcurrentHashMap<>();
            mailingMap.put(mailing,userNotificationMap);
        }
        Notification notification=userNotificationMap.get(entity.getId());
        if(notification==null){
            notification=createNotification(entity,mailing,object);
            userNotificationMap.put(entity.getId(),notification);
        }
        else{
            notification.getObjects().add(object);
        }
    }

    private void updateNotificationInMap(Notification notification){
        Mailing mailing=getMailingOfNotification(notification);
        if(mailing==null) return;
        Map<UUID,Notification> userNotificationMap=mailingMap.get(mailing );
        if((userNotificationMap==null)||(userNotificationMap.get(notification.getTargetEntityUuid())==null)) return;
        userNotificationMap.put(notification.getTargetEntityUuid(),notification);
    }

    private Notification createNotification(StandardEntity entity, Mailing mailing, Object object) {
        Notification notification=metadata.create(Notification.class);
        notification.setMailing(mailing);
        notification.getObjects().add(object);
        notification.setTarget(entity);
        notification.setStage(NotificationStage.CONSOLIDATION);
        notification=dataManager.commit(notification,"notification-full");
        makeRecordOfNotificationStateChange(notification,NotificationStage.CONSOLIDATION);
        actualNotificationMap.put(notification.getId().toString(),notification);
        return notification;
    }

    private void removeNotification(Notification item){
        try{
            mailingMap.get(item.getMailing()).remove(item.getTargetEntityUuid());
        }
        catch (Exception ignored){

        }
    }

    public Collection<Mailing> getAllMailings() {
        return allMailings==null?loadAllMailings():allMailings;
    }



    Collection<StandardEntity> getUsersToNotify(Mailing mailing,Object object) {
        if((mailing.getMailingTargetScript()==null)||(mailing.getMailingTargetScript().size()==0)) return Collections.emptyList();
        Collection<StandardEntity> result=new ArrayList<>();

        Map<String, Object> binding = new HashMap<>();
        binding.put("object", runAdapterOfMailingTargetScript(mailing,object));

        mailing.getMailingTargetScript().forEach(script->{
            try {
                Collection<StandardEntity> currentResult= scripting.evaluateGroovy(script.getScript(), binding);
                result.addAll(currentResult);

            } catch (Exception ignored) {}
        });

        return result;
    }

    private Object runAdapterOfMailingTargetScript(Mailing mailing,Object object){
        if((mailing.getAdapterForMailingTargetScreen()==null)||(StringUtils.isEmpty(mailing.getAdapterForMailingTargetScreen().getScript()))) return object;

        try {
            Map<String, Object> binding = new HashMap<>();
            binding.put("object", object);

            Object result=scripting.evaluateGroovy(mailing.getObjectFilterScript().getScript(), binding);
            return  result;
        } catch (Exception e) {
            return object;
        }
    }

    boolean isAcceptableForObject(Mailing mailing,Object object) {

        if((mailing.getObjectFilterScript()==null)||(StringUtils.isEmpty(mailing.getObjectFilterScript().getScript()))) return true;

        try {
            Map<String, Object> binding = new HashMap<>();
            binding.put("object", object);

            Object result=scripting.evaluateGroovy(mailing.getObjectFilterScript().getScript(), binding);

            return (boolean) result;
        } catch (Exception e) {
            return false;
        }
    }

    TemplateWrapper buildNotificationTemplate(Mailing mailing,Collection<Object> objects){
        if((mailing.getNotificationBuildScript()==null)||(StringUtils.isEmpty(mailing.getNotificationBuildScript().getScript()))) return null;

        try {
            Map<String, Object> binding = new HashMap<>();
            binding.put("objects", objects);

            Object result=scripting.evaluateGroovy(mailing.getNotificationBuildScript().getScript(), binding);

            return (TemplateWrapper) result;
        } catch (Exception e) {
            return null;
        }
    }

    private void dispatchNotification(Notification notification){
        List<AbstractNotificationEvent> notificationEvents=new ArrayList<>();
        Mailing mailing=getMailingOfNotification(notification);
        assert mailing != null;
        for(String performer:  mailing.getMailingPerformers().split(";")){
            try{
                notificationEvents.add(getNotificationEvent(performer));
            }
            catch (Exception e){
                mailingTypeBlackList.add(performer);
            }
        }
        dispatchNotification(notification,notificationEvents);
    }

    private void dispatchNotification(Notification notification,List<AbstractNotificationEvent> notificationEvents){
        try {
            if(notificationEvents.size()==0) return;

            notification.setStage(NotificationStage.AFTER_CONSOLIDATION);
            notification.setSendDate(timeSource.currentTimestamp());
            if(notification.getTemplate()==null){
                notification.setTemplate(buildNotificationTemplate( getMailingOfNotification(notification),notification.getObjects()));
            }

            makeRecordOfNotificationStateChange(notification,NotificationStage.AFTER_CONSOLIDATION);

            StringBuilder channelBuilder=new StringBuilder();
            for(int i=0;i<notificationEvents.size();i++){
                channelBuilder.append(notificationEvents.get(i).getClass().getSimpleName());
                if(i<notificationEvents.size()-1) channelBuilder.append("; ");
            }
            notification.setNotificationChannels(channelBuilder.toString());

            updateNotificationInMap(dataManager.commit(notification,"notification-full"));
            if(notification.getMailing()!=null){
                mailingService.createPersonalSettings(notification.getTargetEntityUuid(),notification.getTargetEntityType(),notification.getMailing());
            }

            notificationEvents.forEach(notificationEvent->{
                notificationEvent.setNotification(notification);
                events.publish(notificationEvent);
            });

            lastSendDateMap.putIfAbsent( getMailingOfNotification(notification),new HashMap<>());
            lastSendDateMap.get( getMailingOfNotification(notification)).put(notification.getTargetEntityUuid(),notification.getSendDate());

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    
    private Mailing getMailingOfNotification(Notification notification){
        if(notification.getMailing()==null) return null;
        for(Mailing mailing:mailingMap.keySet()){
            if(mailing.equals( notification.getMailing())){
                return mailingService.getPersonalizedMailing(mailing,notification.getTargetEntityUuid(),notification.getTargetEntityType());
            }
        }
        return notification.getMailing();
    }



    private Map<String,Class> mailingEventsMap=new HashMap<>();
    private List<String> mailingTypeBlackList=new ArrayList<>();

    private AbstractNotificationEvent getNotificationEvent(String mailingType ) throws Exception {
        //if (mailingTypeBlackList.contains(mailingType)) return null;

        if (mailingEventsMap.get(mailingType) != null) {
            return (AbstractNotificationEvent) mailingEventsMap.get(mailingType).newInstance();
        } else {
            Class eventClass = Class.forName("com.lokoproject.mailing.notification.event." + mailingType+"NotificationEvent");
            mailingEventsMap.put(mailingType, eventClass);
            return (AbstractNotificationEvent) eventClass.newInstance();
        }
    }

    @Override
    public Notification getNotificationById(String id){

        Notification result=actualNotificationMap.get(id);
        if(result!=null){
            return result;
        }
        else {
            return daoService.getNotificationById(id);
        }
    }

    @Override
    public List<Notification> getActualUserNotifications(User user, String notificationAgent) {
        List<Notification> result=new ArrayList<>();
        actualNotificationMap.values().forEach(notification -> {
            if(notification.getStage().equals(NotificationStage.AFTER_CONSOLIDATION)||(notification.getStage().equals(NotificationStage.PROCESSED))){
                if(notification.getTargetEntityUuid().equals(user.getId())){
                    Mailing mailing=getMailingOfNotification(notification);
                    if(mailing==null){
                        if(notification.getNotificationChannels().contains(notificationAgent)){
                            result.add(notification);
                        }
                    }
                    else if( mailing.getMailingPerformers().contains(notificationAgent)){
                        result.add(notification);
                    }
                }
            }
        });

        Collections.sort(result,(n1,n2)-> n1.getSendDate().compareTo(n2.getSendDate()));

        return result;
    }

    @Override
    public void sendNotificationAgain(Notification notification, boolean consolidate) {
        //// TODO: 17.03.2019 придумать, как использовать консолидацию для отправленног уведомления
        Notification notificationCopy=metadata.create(Notification.class);
        notificationCopy = (Notification) EntityUtil.createEntityCopy(notification,notificationCopy,false, Collections.singletonList("id"));
        notificationCopy.setTemplate(notification.getTemplate());
        notificationCopy.setStage(NotificationStage.AFTER_CONSOLIDATION);
        notificationCopy=dataManager.commit(notificationCopy,"notification-full");
        dispatchNotification(notificationCopy);
    }


}

