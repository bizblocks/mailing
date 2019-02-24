package com.lokoproject.mailing.conditions.schedule;

import com.lokoproject.mailing.conditions.AbstractCondition;
import com.lokoproject.mailing.conditions.ConditionException;

import java.util.Date;
import java.util.Map;

/**
 * @author Antonlomako. created on 18.02.2019.
 */
public abstract class AbstractScheduleCondition extends AbstractCondition{

    private Date now;

    protected void fillFieldsFromParamMap(Map<String,Object> params) throws ConditionException {
        try {
            setNow((Date) params.get("now"));
        }
        catch (ClassCastException e){
            ConditionException result=new ConditionException("params must contains now:Date");
            result.initCause(e);
            throw result;
        }
    }

    protected Date getNow() {
        return now;
    }

    protected void setNow(Date now) {
        this.now = now;
    }
}
