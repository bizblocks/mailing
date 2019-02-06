package com.lokoproject.mailing.web.beens.ui;


import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.gui.components.Window;
import com.haulmont.cuba.gui.executors.BackgroundWorker;
import com.haulmont.cuba.gui.executors.UIAccessor;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.cuba.security.global.NoUserSessionException;
import com.vaadin.server.VaadinSession;
import com.vaadin.server.WrappedSession;
import com.vaadin.ui.UIDetachedException;

import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author Antonlomako. created on 13.12.2018.
 */
@Component
public class UiAccessorCollector {

//    @Inject
//    private UserSession userSession= AppBeans.get(UserSession.class);

    @Inject
    private BackgroundWorker backgroundWorker;

    @Inject
    private UserSessionSource userSessionSource;

    private Map<User,Map<String,AccessorWrapper>> uiAccessorMap=new ConcurrentHashMap<>();

    public void addAccessor(Window window, User currentUser){
        addAccessor(window,window.getId(),currentUser);
    }

    public void addAccessor(Window window, String id, User currentUser){

        Map<String,AccessorWrapper> userAccessorMap=uiAccessorMap.get(currentUser);
        if(userAccessorMap==null){
            userAccessorMap=new ConcurrentHashMap<>();
            uiAccessorMap.put(currentUser,userAccessorMap);
        }
        AccessorWrapper accessorWrapper=userAccessorMap.get(id);
        if(accessorWrapper==null){
            accessorWrapper=new AccessorWrapper(window,backgroundWorker.getUIAccessor());
        }
        else {
            accessorWrapper.addWindowAccessor(window,backgroundWorker.getUIAccessor());
        }
        userAccessorMap.put(id,accessorWrapper);
    }

    public void executeFor(Collection<User> targetUsers, String targetScreen, UiOperation uiOperation){
        targetUsers.forEach(item->executeFor(item,targetScreen,uiOperation));
    }

//    public void executeFor(ExecutionTarget executionTarget,String targetScreen,UiOperation uiOperation){
//
//    }

    public void executeFor(User targetUser, String targetScreen, UiOperation uiOperation){
        AccessorWrapper accessorWrapper=getAccessorWrapper(targetUser,targetScreen);
        if(accessorWrapper==null) return;
        List<UIAccessor> accessorsToRemove=new ArrayList<UIAccessor>();

        accessorWrapper.getUiAccessors().forEach(accessor->{
            if(accessorWrapper.canExecute(accessor)) {
                try {
                    accessor.access(() -> {
                        try {
                            uiOperation.doOperation(accessorWrapper.getWindow(accessor));
                        } catch (UIDetachedException exception) {
                            accessorsToRemove.add(accessor);

                        } catch (Exception e) {
                            if (e.getCause() instanceof NoUserSessionException) {
                                accessorsToRemove.add(accessor);

                            }
                        }
                    });
                } catch (UIDetachedException exception) {
                    accessorsToRemove.add(accessor);
                }
            }
            else{
                removeAccessor(targetUser,targetScreen,accessorsToRemove);
            }
        });

        if (accessorsToRemove.size()>0) removeAccessor(targetUser,targetScreen,accessorsToRemove);

    }

    public void executeForAllRegisteredUsers(UiOperation uiOperation){
        uiAccessorMap.forEach((user,accessorWrapperMap)->{
            AccessorWrapper accessorWrapper=accessorWrapperMap.get("main");
            if(accessorWrapper==null) return;

            accessorWrapper.getUiAccessors().forEach(accessor->{
                if(accessorWrapper.canExecute(accessor)) {
                    try {
                        accessor.access(() -> {
                            try {
                                uiOperation.doOperation(accessorWrapper.getWindow(accessor));
                            } catch (Exception e) {

                            }
                        });
                    } catch (UIDetachedException exception) {

                    }
                }
            });
        });
    }

    private void removeAccessor(User targetUser, String targetScreen, List<UIAccessor> accessorsToRemove){
        Map<String,AccessorWrapper> idAccessorMap=uiAccessorMap.get(targetUser);
        if(idAccessorMap!=null){
            AccessorWrapper accessorWrapper=idAccessorMap.get(targetScreen);
            if(accessorWrapper==null) return;

            accessorsToRemove.forEach(item->{
                accessorWrapper.removeAccessor(item);
            });

            if(accessorWrapper.getUiAccessors().size()==0){
                idAccessorMap.remove(targetScreen);
            }

        }
    }

    private AccessorWrapper getAccessorWrapper(User user, String id){
        Map<String,AccessorWrapper> idAccessorMap=uiAccessorMap.get(user);
        if(idAccessorMap==null) return null;
        return idAccessorMap.get(id);
    }

    private class AccessorWrapper{
        private Map<UIAccessor,Window> accessorWindowMap=new ConcurrentHashMap<>();
        private Set<UIAccessor> uiAccessors=new CopyOnWriteArraySet<>();
        private Map<UIAccessor,WrappedSession> accessorSessionMap=new ConcurrentHashMap<>();

        AccessorWrapper(Window window, UIAccessor uiAccessor){
            uiAccessors.add(uiAccessor);
            addWindowAccessor(window,uiAccessor);
        }

        public Window getWindow(UIAccessor uiAccessor) {
            return accessorWindowMap.get(uiAccessor);
        }

        public boolean canExecute(UIAccessor accessor) {
            WrappedSession session =accessorSessionMap.get(accessor);
            if(session==null) return false;
            return true;
            //return HttpSessionCollector.getSessions().get(session.getId())!=null;
        }

        public void addWindowAccessor(Window window, UIAccessor uiAccessor) {
            accessorWindowMap.put(uiAccessor,window);
            accessorSessionMap.put(uiAccessor, VaadinSession.getCurrent().getSession());
            uiAccessors.add(uiAccessor);
        }

        public void removeAccessor(UIAccessor uiAccessor) {
            accessorWindowMap.remove(uiAccessor);
            accessorSessionMap.remove(uiAccessor);
            uiAccessors.remove(uiAccessor);
        }

        public Collection<UIAccessor> getUiAccessors() {
            return uiAccessors;
        }


    }

    public  interface UiOperation{

         void doOperation(Window window);
    }

    public enum ExecutionTarget{
        ALL,ALL_EXCEPT_CURRENT
    }

}
