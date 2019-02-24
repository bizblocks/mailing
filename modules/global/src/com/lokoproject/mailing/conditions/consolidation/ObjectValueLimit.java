package com.lokoproject.mailing.conditions.consolidation;

import com.lokoproject.mailing.conditions.Condition;
import com.lokoproject.mailing.conditions.ConditionException;

import java.util.Map;

/**
 * @author Antonlomako. created on 15.02.2019.
 */
public class ObjectValueLimit extends AbstractConsolidationCondition {

    @Condition.FieldDescription(type = FieldDescription.ParameterType.INTEGER)
    Integer objectLimit;

    @Override
    public boolean check(Map<String, Object> params) throws ConditionException {
        fillFieldsFromParamMap(params);
        return getObjectsValue() >objectLimit;
    }

    @Override
    public String makeStateDescription() {
        if(objectLimit==null) return null;
        return String.valueOf(objectLimit);
    }
}
