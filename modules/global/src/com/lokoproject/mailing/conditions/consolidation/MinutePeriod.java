package com.lokoproject.mailing.conditions.consolidation;

import com.lokoproject.mailing.conditions.Condition;
import com.lokoproject.mailing.conditions.ConditionException;

import java.util.Map;

/**
 * @author Antonlomako. created on 15.02.2019.
 */
public class MinutePeriod extends AbstractConsolidationCondition {

    @Condition.FieldDescription(type = FieldDescription.ParameterType.INTEGER,name = "period, min")
    Integer period;

    @Override
    public boolean check(Map<String, Object> params) throws ConditionException {

        fillFieldsFromParamMap(params);

        long timePassedMs= getNow().getTime()- getLastSendDate().getTime();
        return (timePassedMs/1000/60>=period);
    }

    @Override
    public String makeStateDescription() {
        if (period==null) return null;
        return String.valueOf(period)+" min";
    }
}
