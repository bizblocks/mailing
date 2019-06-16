package com.lokoproject.mailing.listener;

import com.lokoproject.mailing.service.MailingService;
import org.springframework.stereotype.Component;
import com.haulmont.cuba.core.listener.BeforeDetachEntityListener;
import com.haulmont.cuba.core.EntityManager;
import com.lokoproject.mailing.entity.GroovyScript;
import com.haulmont.cuba.core.listener.BeforeAttachEntityListener;

import javax.inject.Inject;

@Component("mailing_GroovyScriptEntityListener")
public class GroovyScriptEntityListener implements BeforeDetachEntityListener<GroovyScript>, BeforeAttachEntityListener<GroovyScript> {

    @Inject
    private MailingService mailingService;

    @Override
    public void onBeforeDetach(GroovyScript entity, EntityManager entityManager) {

    }


    @Override
    public void onBeforeAttach(GroovyScript entity) {
        mailingService.updateAllMailings();
    }


}