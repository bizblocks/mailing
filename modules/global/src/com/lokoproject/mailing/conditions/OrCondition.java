package com.lokoproject.mailing.conditions;

import java.util.Map;

/**
 * @author Antonlomako. created on 18.02.2019.
 */
public final class OrCondition extends AbstractConditionGroup {


    @Override
    public boolean check(Map<String, Object> params) throws ConditionException {
        if((children==null)||(children.size()==0)) throw new ConditionException("empty OR condition");
        for(Condition childCondition:children){
            if(childCondition.check(params)) return true;
        }
        return false;
    }

    @Override
    public String makeStateDescription() {
        return null;
    }
}
