package com.lokoproject.mailing.core.notification.performer;

import com.haulmont.cuba.core.app.EmailService;
import com.haulmont.cuba.core.global.EmailInfo;
import com.haulmont.cuba.security.entity.User;
import com.lokoproject.mailing.core.notification.NotificationAgentException;
import com.lokoproject.mailing.notification.template.Template;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;


/**
 * @author Antonlomako. created on 09.12.2018.
 */
public class CubaEmailNotificationPerformer {

    @Inject
    private EmailService emailService;


    public void sendNotificationByTemplate(User target, Template template) throws NotificationAgentException {

        String email=target.getEmail();
        if(StringUtils.isBlank(email)) throw new NotificationAgentException("no email for user: "+target.getLogin());

        EmailInfo emailInfo = new EmailInfo(
                email,
                template.getTheme(),
                ""
        );
        emailInfo.setBodyContentType("text/html; charset=UTF-8");
        emailService.sendEmailAsync(emailInfo);

        return ;
    }
}
