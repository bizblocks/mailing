package com.lokoproject.mailing.web.screens;

import com.haulmont.bali.util.ParamsMap;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.AbstractWindow;
import com.haulmont.cuba.gui.components.TextField;
import com.haulmont.cuba.security.global.UserSession;
import com.lokoproject.mailing.notification.template.TemplateBuilder;
import com.lokoproject.mailing.notification.template.element.TemplateElement;
import com.lokoproject.mailing.service.BotService;
import com.lokoproject.mailing.service.NotificationService;
import com.lokoproject.mailing.web.beens.notification.CubaWebClientNotificationPerformer;

import javax.inject.Inject;
import java.util.*;

public class Screen extends AbstractWindow {

    @Inject
    private TextField textField;

    @Inject
    private NotificationService notificationService;

    @Inject
    private CubaWebClientNotificationPerformer notificationProcessor;

    @Inject
    private BotService botService;

    @Inject
    private UserSession userSession;

    public void onStrClick() {
        notificationProcessor.showDesktopNotificationToUser(userSession.getUser(),"header","content","",window->{
            showNotification("click");
        });
        //notificationService.addNotification(textField.getValue());
    }

    public void onIntClick() {
//        notificationService.addNotification(Integer.valueOf(textField.getValue()));
        List<Map<String,String>> data=new ArrayList<>();

        for(int i=0;i<5;i++){
            Map<String,String> row=new HashMap<>();
            for(int j=0;j<3;j++){
                row.put("col"+String.valueOf(j),String.valueOf(j+i));
            }
            data.add(row);
        }
        TemplateElement template=TemplateBuilder.createTemplateBuilder()
                .withChild(TemplateBuilder.createTableBuilder(data)
                        .withColumns(Arrays.asList("col0","col1","col2"))
                        .build())
                .withChild(TemplateBuilder.createList(Arrays.asList("col0","col1","col2")))
                .build();
        openWindow("notificationTemplateProcessor", WindowManager.OpenType.DIALOG, ParamsMap.of("notificationTemplate",template));
    }

    public void onSClick() {
        notificationService.addNotification(textField.getValue());
    }

    public void onIClick() {
        notificationService.addNotification(Integer.valueOf(textField.getValue()));
    }

    public void onBotClick() {
        botService.startBot();
    }
}