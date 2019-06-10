package com.lokoproject.mailing.core.notification.performer;

import com.haulmont.cuba.core.app.EmailService;
import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.cuba.core.global.EmailInfo;
import com.lokoproject.mailing.core.notification.NotificationAgentException;
import com.lokoproject.mailing.notification.event.CubaEmailNotificationEvent;
import com.lokoproject.mailing.notification.template.TemplateWrapper;
import com.lokoproject.mailing.notification.template.element.*;
import com.lokoproject.mailing.service.IdentifierService;
import com.lokoproject.mailing.service.NotificationService;
import com.lokoproject.mailing.utils.HtmlTemplateHelper;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;


/**
 * @author Antonlomako. created on 09.12.2018.
 */
@Component
public class CubaEmailNotificationPerformer implements ApplicationListener<CubaEmailNotificationEvent>{

    @Inject
    private EmailService emailService;

    @Inject
    private NotificationService notificationService;

    @Inject
    private IdentifierService identifierService;

    private String watchReceiptMethodUrl="/app/rest/v2/services/mailing_NotificationService/confirmNotificationReceipt?notificationId=";


    public void sendNotificationByTemplate(String email, TemplateElement templateWrapper,String notificationIdToWatchReceipt) throws NotificationAgentException {

        String mailContent=HtmlTemplateHelper.buildTemplateRecur(null,templateWrapper).toString();
        if(notificationIdToWatchReceipt!=null){
            mailContent=mailContent+"\n"+
                    "Пожалуйста, перейдите по ссылке, чтобы подтвердить получение письма";

        }

        EmailInfo emailInfo = new EmailInfo(
                email,
                (templateWrapper instanceof TemplateWrapper)? ((TemplateWrapper)templateWrapper).getTheme():"",
                mailContent
        );
        emailInfo.setBodyContentType("text/html; charset=UTF-8");
        emailService.sendEmailAsync(emailInfo);
    }
    @Override
    public void onApplicationEvent(CubaEmailNotificationEvent event) {
        try {
            String email=identifierService.getIdentifier(event.getNotification().getTargetEntityType(),
                    event.getNotification().getTargetEntityUuid().toString(),
                    "CubaEmail");

            sendNotificationByTemplate(email,event.getNotification().getTemplate(),null);
        } catch (NotificationAgentException e) {
            e.printStackTrace();
        }
    }

}
