package com.lokoproject.mailing.service;

import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.security.entity.User;
import com.lokoproject.mailing.entity.MailingIdentifier;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.UUID;

@Service(DaoService.NAME)
public class DaoServiceBean implements DaoService {

    @Inject
    private DataManager dataManager;

    @Inject
    private com.haulmont.cuba.core.Persistence persistence;

    @Override
    public User getUserByLogin(String login){
        LoadContext loadContext = LoadContext.create(User.class)
                .setQuery(LoadContext.createQuery("select e from sec$User e where e.login=:loginItem")
                        .setParameter("loginItem",login ))
                ;
        User result=(User) dataManager.load(loadContext);
        return result;
    }

    @Override
    public StandardEntity getEntity(String entityType, String entityUUID) {
        if((entityType==null)||(entityUUID==null))return null;

        Transaction tx = persistence.createTransaction();
        Object en;
        try {
            EntityManager em = persistence.getEntityManager();
            en = em.createQuery(
                    "SELECT e FROM "  + entityType + " e where e.id=:id")
                    .setParameter("id", UUID.fromString(entityUUID))
                    .getSingleResult();


            tx.commit();
        }
        finally {
            tx.end();
        }

        return (StandardEntity)en;
    }

    @Override
    public MailingIdentifier getIdentifier(String keyString, String identifierName) {
        LoadContext loadContext = LoadContext.create(MailingIdentifier.class)
                .setQuery(LoadContext.createQuery("select e from mailing$MailingIdentifier e where " +
                        "e.objectId=:idItem " +
                        "and e.identifierName=:nameItem")
                        .setParameter("idItem",keyString )
                        .setParameter("nameItem",identifierName))
                ;
        MailingIdentifier result=(MailingIdentifier) dataManager.load(loadContext);
        return result;
    }

}