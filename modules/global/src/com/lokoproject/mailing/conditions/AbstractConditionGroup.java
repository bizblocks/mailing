package com.lokoproject.mailing.conditions;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Antonlomako. created on 18.02.2019.
 */
public abstract class AbstractConditionGroup extends AbstractCondition implements ConditionGroup {

    protected List<Condition> children=new ArrayList<>();

    @Override
    public List<Condition> getChildren(){
        return children;
    }

    @Override
    public void addChild(Condition child){
        children.add(child);
    }
}
