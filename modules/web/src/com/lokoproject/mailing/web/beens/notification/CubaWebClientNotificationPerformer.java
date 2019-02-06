package com.lokoproject.mailing.web.beens.notification;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.gui.components.Frame;
import com.haulmont.cuba.gui.components.Window;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.cuba.security.global.UserSession;
import com.lokoproject.mailing.entity.NotificationStage;
import com.lokoproject.mailing.notification.event.CubaWebClientNotificationEvent;
import com.lokoproject.mailing.service.NotificationService;
import com.lokoproject.mailing.web.beens.ui.UiAccessorCollector;
import com.lokoproject.mailing.web.screens.Notificationbell;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.JavaScriptFunction;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * @author Antonlomako. created on 02.02.2019.
 */
@Component
public class CubaWebClientNotificationPerformer implements ApplicationListener<CubaWebClientNotificationEvent> {

    @Inject
    private UiAccessorCollector uiAccessorCollector;

    @Inject
    private NotificationService notificationService;


    @Override
    public void onApplicationEvent(CubaWebClientNotificationEvent event) {
        notificationService.updateNotificationStage(event.getNotification(), NotificationStage.PROCESSED);
        showDesktopNotificationToUser(event.getNotification().getTarget(),"header","content","icon",null);
    }

    public interface NotificationClickListener{
        void onClick(Window window);
    }

    public void showDesktopNotificationToUser(User user, String header, String content,String icon,NotificationClickListener clickListener){

        uiAccessorCollector.executeFor(user,"main",(window)->{

            if(clickListener!=null){
                JavaScript.getCurrent().addFunction("onNotificationClick", (JavaScriptFunction) arguments -> clickListener.onClick(window));
            }

            String jsFunction="if (!Notification) {\n" +
                    "            alert('Desktop notifications not available in your browser. Try Chromium.');\n" +
                    "            return;\n" +
                    "        }\n" +
                    "        if (Notification.permission !== \"granted\")\n" +
                    "            Notification.requestPermission();\n" +
                    "        else {\n" +
                    "            var notification = new Notification('$header', {\n" +
                    "                    icon: '$icon',\n" +
                    "                    body: '$content',\n" +
                    "            });\n" +
                    "           $clickFunction"+
                    "        }";

            String clickFunction="            notification.onclick = function () {\n" +
                    "                onNotificationClick();\n" +
                    "            };\n" ;

            jsFunction=jsFunction.replace("$header",header);
            jsFunction=jsFunction.replace("$icon",icon);
            jsFunction=jsFunction.replace("$content",content);
            if(clickListener!=null){
                jsFunction=jsFunction.replace("$clickFunction",clickFunction);
            }
            else{
                jsFunction=jsFunction.replace("$clickFunction","");
            }

            JavaScript.getCurrent().execute(jsFunction);

        });

        uiAccessorCollector.executeFor(user,"notification",(window -> {
            if(window instanceof Notificationbell){
                Notificationbell notification= (Notificationbell) window;
                notification.increaseUnreadNotificationValue();
            }
        }));

    }

    public void increaseUnreadNotificationValueForUser(User user){

    }

    public void resetUnreadNotificationValueForUser(User user){

    }

    public void setUnreadNotificationValueForUser(User user,Integer value){

    }

    public void showWebNotificationForUser(User user, String header, String content, Frame.NotificationType notificationType){

    }
}
