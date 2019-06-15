package com.lokoproject.mailing.dto;

import com.haulmont.cuba.core.entity.StandardEntity;
import com.lokoproject.mailing.entity.Mailing;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Anton on 14.06.2019.
 */
public class ResultOfAddingNotification implements Serializable {
    private Map<Mailing,Set<StandardEntity>> mailingTargetsMap=new HashMap<>();

    public Map<Mailing, Set<StandardEntity>> getMailingTargetsMap() {
        return mailingTargetsMap;
    }

    public void setMailingTargetsMap(Map<Mailing, Set<StandardEntity>> mailingTargetsMap) {
        this.mailingTargetsMap = mailingTargetsMap;
    }

    public void addMailingTarget(Mailing mailing,StandardEntity entity){
        mailingTargetsMap.putIfAbsent(mailing,new HashSet<>());
        mailingTargetsMap.get(mailing).add(entity);
    }

    public void joinResults(ResultOfAddingNotification otherResult){
        otherResult.getMailingTargetsMap().forEach((mailing,entitySet)->{
            mailingTargetsMap.putIfAbsent(mailing,entitySet);
            mailingTargetsMap.get(mailing).addAll(entitySet);
        });

    }
}
