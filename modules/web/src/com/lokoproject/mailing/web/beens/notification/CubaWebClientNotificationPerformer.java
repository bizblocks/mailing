package com.lokoproject.mailing.web.beens.notification;

import com.haulmont.bali.util.ParamsMap;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.core.sys.SecurityContext;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.Frame;
import com.haulmont.cuba.gui.components.HBoxLayout;
import com.haulmont.cuba.gui.components.Window;
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.cuba.security.global.UserSession;
import com.lokoproject.mailing.entity.Notification;
import com.lokoproject.mailing.entity.NotificationStage;
import com.lokoproject.mailing.notification.event.CubaWebClientNotificationEvent;
import com.lokoproject.mailing.service.NotificationService;
import com.lokoproject.mailing.web.beens.ui.UiAccessorCollector;
import com.lokoproject.mailing.web.notification.UserNotification;
import com.lokoproject.mailing.web.screens.Notificationbell;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinServletRequest;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.JavaScriptFunction;
import elemental.json.JsonArray;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;

/**
 * @author Antonlomako. created on 02.02.2019.
 */
@Component
public class CubaWebClientNotificationPerformer implements ApplicationListener<CubaWebClientNotificationEvent> {

    @Inject
    private UiAccessorCollector uiAccessorCollector;

    @Inject
    private NotificationService notificationService;


    private UserSession userSession;
    private ComponentsFactory componentsFactory;


    @Override
    public void onApplicationEvent(CubaWebClientNotificationEvent event) {

        showDesktopNotificationToUser(event.getNotification().getTarget()
                ,event.getNotification().getTemplate().getDescription()
                ,event.getNotification().getTemplate().getTheme()
                ,event.getNotification().getTemplate().getIconName()
                ,event.getNotification().getId().toString()
                ,(window,arguments) -> {
                    Notification notification=notificationService.getNotificationById(arguments.getString(0));
                    String windowHash=arguments.getString(1);
                    onNotificationClick(notification,windowHash);
                }
        );

        uiAccessorCollector.executeFor(event.getNotification().getTarget(),"notification",(window -> {
            if(window instanceof Notificationbell){
                Notificationbell notification= (Notificationbell) window;
                notification.addNotification(event.getNotification());
            }
        }));

        uiAccessorCollector.executeFor(event.getNotification().getTarget(),"userNotification",(window -> {
            if(window instanceof UserNotification){
                UserNotification userNotification= (UserNotification) window;
                userNotification.updateNotificationStageInTable(event.getNotification());
            }
        }));

        uiAccessorCollector.executeFor(event.getNotification().getTarget(),"main",(window)->{
            notificationService.updateNotificationStage(event.getNotification(), NotificationStage.PROCESSED);
            // TODO: 08.02.2019 перенести в отдельный метод и выполнять асинхронно
        });
    }

    public void onNotificationClick(Notification notification, String windowHash){
        if(notification!=null){

            markNotificationAsRead(notification);

            uiAccessorCollector.executeOnConcreteScreenFor(getUserSession().getUser(),"main",windowHash,(window)->{
                window.openWindow("notificationTemplateProcessor", WindowManager.OpenType.DIALOG, ParamsMap.of("notificationTemplate",notification.getTemplate() ));
            });

        }
    }

    public void markNotificationAsRead(Notification notification){

        uiAccessorCollector.executeOnceFor(getUserSession().getUser(),"main",(window)->{
            Notification modifiedNotification= notificationService.updateNotificationStage(notification,NotificationStage.READ);
            notification.setStage(NotificationStage.READ);

            uiAccessorCollector.executeFor(getUserSession().getUser(),"notification",(bellWindow)->{
                if(bellWindow instanceof Notificationbell){
                    Notificationbell bell= (Notificationbell) bellWindow;
                    bell.removeNotification(modifiedNotification);
                }
            });

            uiAccessorCollector.executeFor(getUserSession().getUser(),"userNotification",(userNotificationWindow)->{
                if(userNotificationWindow instanceof UserNotification){
                    UserNotification userNotification= (UserNotification) userNotificationWindow;
                    userNotification.updateNotificationStageInTable(modifiedNotification);
                }
            });
        });




    }

    public UserSession getUserSession() {
        SecurityContext securityContext=AppContext.getSecurityContext();
        if(securityContext==null) return null;
        return securityContext.getSession();
    }

    public ComponentsFactory getComponentsFactory() {
        if(componentsFactory==null) componentsFactory=AppBeans.get(ComponentsFactory.class);
        return componentsFactory;
    }

    public interface NotificationClickListener{
        void onClick(Window window, JsonArray arguments);
    }

    public void showDesktopNotificationToUser(User user, String header, String content,String icon,String clickParameter,NotificationClickListener clickListener){

        uiAccessorCollector.executeOnceFor(user,"main",(window)->{
            if(clickListener!=null){
                JavaScript.getCurrent().addFunction("onNotificationClick", (JavaScriptFunction) arguments -> clickListener.onClick(window,  arguments));
            }

            String jsFunction="if (!Notification) {\n" +
                    "alert('Desktop notifications not available in your browser. Try Chromium.');\n" +
                    "return;\n" +
                    "}\n" +
                    "if (Notification.permission !== \"granted\")\n" +
                    "Notification.requestPermission();\n" +
                    "else {\n" +
                    "var notification = new Notification('$header', {\n" +
                    "icon: '$icon',\n" +
                    "body: '$content',\n" +
                    "});\n" +
                    "$clickFunction"+
                    "}";

            String clickFunction="            notification.onclick = function () {\n" +
                    "                onNotificationClick('$clickParam1','$clickParam2');\n" +
                    "            };\n" ;

            jsFunction=jsFunction.replace("$header",header);
            jsFunction=jsFunction.replace("$icon",icon);
            jsFunction=jsFunction.replace("$content",content);
            clickFunction=clickFunction.replace("$clickParam1",clickParameter);
            clickFunction=clickFunction.replace("$clickParam2",window.toString());
            if(clickListener!=null){
                jsFunction=jsFunction.replace("$clickFunction",clickFunction);
            }
            else{
                jsFunction=jsFunction.replace("$clickFunction","");
            }

            JavaScript.getCurrent().execute(jsFunction);

        });



    }

    public void initByMainWindow(Window window){
        String url = ((VaadinServletRequest) VaadinService.getCurrentRequest()).getServerName()+":"+
                ((VaadinServletRequest) VaadinService.getCurrentRequest()).getServerPort();

        User currentUser = getUserSession().getUser();
        uiAccessorCollector.addAccessor(window,"main",currentUser);

        List<Notification> actualUserNotifications=notificationService.getActualUserNotifications(currentUser,"CubaWebClient");

        Frame frame= getComponentsFactory().createComponent(Frame.class);
        Notificationbell bell= (Notificationbell) window.openFrame(frame,"notificationBell");
        bell.setMainWindowHash(window.toString());
        bell.setUnreadNotifications(actualUserNotifications);

        frame.add(bell);

        HBoxLayout hBoxLayout= (HBoxLayout) window.getComponent("titleBar");
        if(hBoxLayout!=null){
            hBoxLayout.add(frame,2);
        }
        frame.setWidth("50px");
        frame.setHeight("35px");
        uiAccessorCollector.addAccessor(bell,"notification",currentUser);
    }
}
