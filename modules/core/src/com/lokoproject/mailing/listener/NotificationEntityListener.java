package com.lokoproject.mailing.listener;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lokoproject.mailing.notification.template.TemplateWrapper;
import org.springframework.stereotype.Component;
import com.haulmont.cuba.core.listener.BeforeDetachEntityListener;
import com.haulmont.cuba.core.EntityManager;
import com.lokoproject.mailing.entity.Notification;

import java.io.IOException;
import java.sql.Connection;
import com.haulmont.cuba.core.listener.BeforeAttachEntityListener;

@Component("mailing_NotificationEntityListener")
public class NotificationEntityListener implements BeforeDetachEntityListener<Notification>,  BeforeAttachEntityListener<Notification> {


    @Override
    public void onBeforeDetach(Notification entity, EntityManager entityManager) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.WRAPPER_OBJECT);
        try {
            if(entity.getTemplateJson()==null) return;
            TemplateWrapper templateWrapper=mapper.readValue(entity.getTemplateJson(), TemplateWrapper.class);
            entity.setTemplate(templateWrapper);
        } catch (IOException ignored) {
            ignored.printStackTrace();
        }
    }

    @Override
    public void onBeforeAttach(Notification entity) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.WRAPPER_OBJECT);
        try {
            if(entity.getTemplate()!=null) {
                String templateJson = mapper.writeValueAsString(entity.getTemplate());
                entity.setTemplateJson(templateJson);
            }
        } catch (JsonProcessingException ignored) {}
    }


}