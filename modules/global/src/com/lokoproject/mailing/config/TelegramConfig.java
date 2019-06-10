package com.lokoproject.mailing.config;

import com.haulmont.cuba.core.config.Config;
import com.haulmont.cuba.core.config.Property;
import com.haulmont.cuba.core.config.Source;
import com.haulmont.cuba.core.config.SourceType;
import com.lokoproject.mailing.entity.Mailing;

import java.util.List;


/**
 * Created by Anton on 02.06.2019.
 */
@Source(type = SourceType.DATABASE)
public interface TelegramConfig extends Config {

    @Property("telegramConfig.notificationBotToken")
    String getNotificationBotToken();

    @Property("telegramConfig.notificationBotName")
    String getNotificationBotName();

    @Property("telegram.entityTypesWithPasswordAuthorization")
    String getEntityTypesWithPasswordAuthorization();

    @Property("telegram.mailingToRequestPassword")
    Mailing getMailingToRequestPassword();

}
