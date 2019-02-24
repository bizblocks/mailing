package com.lokoproject.mailing.conditions;

import java.util.List;

/**
 * @author Antonlomako. created on 18.02.2019.
 */
public interface ConditionGroup extends Condition {

    List<Condition> getChildren();
    void addChild(Condition child);
}
