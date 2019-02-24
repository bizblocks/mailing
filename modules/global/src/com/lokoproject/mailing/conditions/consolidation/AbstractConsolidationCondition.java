package com.lokoproject.mailing.conditions.consolidation;

import com.lokoproject.mailing.conditions.AbstractCondition;
import com.lokoproject.mailing.conditions.ConditionException;

import java.util.Date;
import java.util.Map;

/**
 * @author Antonlomako. created on 19.02.2019.
 */
public abstract class AbstractConsolidationCondition extends AbstractCondition {

    private Date now;
    private Date lastSendDate;
    private Integer objectsValue;

    protected void fillFieldsFromParamMap(Map<String,Object> params) throws ConditionException {
        try {
            setLastSendDate((Date) params.get("lastSendDate"));
            setObjectsValue((Integer) params.get("objectsValue"));
            setNow((Date) params.get("now"));
        }
        catch (ClassCastException e){
            ConditionException result=new ConditionException("params must contains lastSendDate:Date, objectsValue:Integer and now:Date");
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

    protected Date getLastSendDate() {
        return lastSendDate;
    }

    protected void setLastSendDate(Date lastSendDate) {
        this.lastSendDate = lastSendDate;
    }

    protected Integer getObjectsValue() {
        return objectsValue;
    }

    protected void setObjectsValue(Integer objectsValue) {
        this.objectsValue = objectsValue;
    }
}
