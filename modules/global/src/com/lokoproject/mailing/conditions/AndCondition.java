package com.lokoproject.mailing.conditions;

import java.util.Map;

/**
 * @author Antonlomako. created on 18.02.2019.
 */
public final class AndCondition extends AbstractConditionGroup {


    @Override
    public boolean check(Map<String, Object> params) throws ConditionException {
        if((children==null)||(children.size()==0)) throw new ConditionException("empty AND condition");
        for(Condition childCondition:children){
            if(!childCondition.check(params)) return false;
        }
        return true;
    }

    @Override
    public String makeStateDescription() {
        return null;
    }
}
