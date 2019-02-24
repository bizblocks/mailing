package com.lokoproject.mailing.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.haulmont.bali.util.ParamsMap;
import com.haulmont.cuba.core.entity.contracts.Id;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.security.app.Authenticated;
import com.haulmont.cuba.security.entity.User;
import com.lokoproject.mailing.conditions.ConditionException;
import com.lokoproject.mailing.entity.NotificationStage;
import com.lokoproject.mailing.entity.NotificationStageLog;
import com.lokoproject.mailing.notification.event.AbstractNotificationEvent;
import com.lokoproject.mailing.entity.Mailing;
import com.lokoproject.mailing.entity.Notification;
import com.lokoproject.mailing.notification.template.TemplateWrapper;
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
    protected Scripting scripting;


    private Map<Mailing,Map<User,Notification>> mailingMap=new ConcurrentHashMap<>();
    private Map<Mailing,Map<User,Date>> lastSendDateMap=new ConcurrentHashMap<>();
    private Map<String,Notification> actualNotificationMap=new ConcurrentHashMap<>();
    private Collection<Mailing> allMailings;

    @Override
    public void updateMailing(Mailing mailing){
        if(getAllMailings()==null) return;

        getAllMailings().remove(mailing);
        getAllMailings().add(mailing);
    }

    @Override
    public void addNotification(Object object){

        Collection<Mailing> mailings=getAllMailings();
        mailings.forEach(mailing->{
            if(isAcceptableForObject(mailing,object)){
                getUsersToNotify(mailing,object).forEach(user->{
                    addObjectToNotification(user,mailing,object);
                });
            }
        });
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
        Mailing mailing=notification.getMailing();
        if((mailing.getConsolidationCondition()==null)&&(mailing.getConsolidationGroovy()==null)) return true;

        Date lastSendDate=null;
        Map<User,Date> userDateMap=lastSendDateMap.get(notification.getMailing());
        if(userDateMap!=null){
            lastSendDate=userDateMap.get(notification.getTarget());
        }
        if(lastSendDate==null){
            lastSendDate=now;   //чтобы при запуске системы сразу накапливаемые уведомления не отправлялись
        }

        if(mailing.getConsolidationCondition()!=null){

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
                .setQuery(LoadContext.createQuery("select m from mailing$Mailing m") )
                .setView("mailing-full");
        ;
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



    private void addObjectToNotification(User user, Mailing mailing, Object object) {
        Map<User,Notification> userNotificationMap=mailingMap.get(mailing);
        if(userNotificationMap==null){
            userNotificationMap=new ConcurrentHashMap<>();
            mailingMap.put(mailing,userNotificationMap);
        }
        Notification notification=userNotificationMap.get(user);
        if(notification==null){
            notification=createNotification(user,mailing,object);
            userNotificationMap.put(user,notification);
        }
        else{
            notification.getObjects().add(object);
        }
    }

    private void updateNotificationInMap(Notification notification){
        Map<User,Notification> userNotificationMap=mailingMap.get(notification.getMailing());
        if((userNotificationMap==null)||(userNotificationMap.get(notification.getTarget())==null)) return;
        userNotificationMap.put(notification.getTarget(),notification);
    }

    private Notification createNotification(User user, Mailing mailing, Object object) {
        Notification notification=metadata.create(Notification.class);
        notification.setMailing(mailing);
        notification.getObjects().add(object);
        notification.setTarget(user);
        notification.setStage(NotificationStage.CONSOLIDATION);
        notification=dataManager.commit(notification,"notification-full");
        makeRecordOfNotificationStateChange(notification,NotificationStage.CONSOLIDATION);
        actualNotificationMap.put(notification.getId().toString(),notification);
        return notification;
    }

    private void removeNotification(Notification item){
        try{
            mailingMap.get(item.getMailing()).remove(item.getTarget());
        }
        catch (Exception ignored){

        }
    }

    public Collection<Mailing> getAllMailings() {
        return allMailings==null?loadAllMailings():allMailings;
    }



    Collection<User> getUsersToNotify(Mailing mailing,Object object) {
        if((mailing.getMailingTargetScript()==null)||(StringUtils.isEmpty(mailing.getMailingTargetScript().getScript()))) return Collections.emptyList();

        try {
            Map<String, Object> binding = new HashMap<>();
            binding.put("object", object);

            return (Collection<User>) scripting.evaluateGroovy(mailing.getMailingTargetScript().getScript(), binding);

        } catch (Exception e) {
            return Collections.emptyList();
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
        try {

            List<AbstractNotificationEvent> notificationEvents=new ArrayList<>();
            for(String performer: notification.getMailing().getMailingPerformers().split(";")){
                try{
                    notificationEvents.add(getNotificationEvent(performer));
                }
                catch (Exception ignored){}
            }
            if(notificationEvents.size()==0) return;

            notification.setStage(NotificationStage.AFTER_CONSOLIDATION);
            notification.setSendDate(timeSource.currentTimestamp());
            notification.setTemplate(buildNotificationTemplate(notification.getMailing(),notification.getObjects()));

            makeRecordOfNotificationStateChange(notification,NotificationStage.AFTER_CONSOLIDATION);
            updateNotificationInMap(dataManager.commit(notification,"notification-full"));

            notificationEvents.forEach(notificationEvent->{
                notificationEvent.setNotification(notification);
                events.publish(notificationEvent);
            });

            lastSendDateMap.putIfAbsent(notification.getMailing(),new HashMap<>());
            lastSendDateMap.get(notification.getMailing()).put(notification.getTarget(),notification.getSendDate());

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private Map<String,Class> mailingEventsMap=new HashMap<>();
    private List<String> mailingTypeBlackList=new ArrayList<>();

    private AbstractNotificationEvent getNotificationEvent(String mailingType ) throws Exception {
        if (mailingTypeBlackList.contains(mailingType)) return null;

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
                if(notification.getTarget().equals(user)){
                    if(notification.getMailing().getMailingPerformers().contains(notificationAgent)){
                        result.add(notification);
                    }
                }
            }
        });

        Collections.sort(result,(n1,n2)-> n1.getSendDate().compareTo(n2.getSendDate()));

        return result;
    }


}

