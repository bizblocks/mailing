package com.lokoproject.mailing.notification.event;

/**
 * @author Antonlomako. created on 15.12.2018.
 */
public class CubaWebClientNotificationEvent extends AbstractNotificationEvent implements InstantWebEvent {

    public CubaWebClientNotificationEvent(){
        super("cuba_web_client_notification_event");
    }

    @Override
    public String getDefaultIdentifierName() {
        return "login";
    }
}
