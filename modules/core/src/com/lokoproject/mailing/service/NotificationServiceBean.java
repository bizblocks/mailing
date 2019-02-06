package com.lokoproject.mailing.service;

import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.security.entity.User;
import com.lokoproject.mailing.entity.NotificationStage;
import com.lokoproject.mailing.entity.NotificationStageLog;
import com.lokoproject.mailing.notification.event.AbstractNotificationEvent;
import com.lokoproject.mailing.entity.Mailing;
import com.lokoproject.mailing.entity.Notification;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;


import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
    private Collection<Mailing> allMailings;

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

    private boolean canSendNow(Notification notification, Date date) {
        return true;
    }

    @Override
    public Collection<Mailing> loadAllMailings() {
        LoadContext<Mailing> loadContext = LoadContext.create(Mailing.class)
                .setQuery(LoadContext.createQuery("select m from mailing$Mailing m") )
                .setView("mailing-full");
        ;
        allMailings= dataManager.loadList(loadContext);
        return allMailings;
    }

    @Override
    public Notification updateNotificationStage(Notification notification, NotificationStage stage){
        notification=dataManager.reload(notification,"_local");
        notification.setStage(stage);
        notification=dataManager.commit(notification);
        makeRecordOfNotificationStateChange(notification,stage);
        return notification;
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
        notification=dataManager.commit(notification);
        makeRecordOfNotificationStateChange(notification,NotificationStage.CONSOLIDATION);
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
        //return allMailings==null?loadAllMailings():allMailings;
        return loadAllMailings();
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

    private void dispatchNotification(Notification notification){
        try {
            AbstractNotificationEvent notificationEvent= getNotificationEvent(notification.getMailing().getMailingAgents());

            notification.setStage(NotificationStage.AFTER_CONSOLIDATION);
            notification.setSendDate(timeSource.currentTimestamp());
            notification=dataManager.commit(notification);
            makeRecordOfNotificationStateChange(notification,NotificationStage.AFTER_CONSOLIDATION);
            updateNotificationInMap(notification);

            notificationEvent.setNotification(notification);
            events.publish(notificationEvent);
            lastSendNotification=notification;
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



    Notification lastSendNotification;
    @Override
    public Collection<Notification> getNotifications(String ids){
        return Arrays.asList(lastSendNotification);
    }
}

