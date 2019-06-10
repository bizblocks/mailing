package com.lokoproject.mailing.service;


import com.lokoproject.mailing.entity.Mailing;

import java.util.UUID;

public interface MailingService {
    String NAME = "mailing_MailingService";

    Mailing getPersonalizedMailing(Mailing mailing, UUID targetEntityUuid, String targetEntityType);

    Mailing getMailingById(String id);

    Mailing createPersonalSettings(UUID entityId, String entityType, Mailing originMailing);

    void updateMailing(Mailing mailing);

    void onRemoveMailing(Mailing mailing);
}